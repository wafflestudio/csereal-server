package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.utils.isCurrentUserStaff
import com.wafflestudio.csereal.core.reservation.config.ReservationProperties
import com.wafflestudio.csereal.core.reservation.database.ReservationEntity
import com.wafflestudio.csereal.core.reservation.database.ReservationRepository
import com.wafflestudio.csereal.core.reservation.database.ReservationType
import com.wafflestudio.csereal.core.reservation.database.RoomRepository
import com.wafflestudio.csereal.core.reservation.database.RoomType
import com.wafflestudio.csereal.core.reservation.dto.ReservationDto
import com.wafflestudio.csereal.core.reservation.dto.ReserveRequest
import com.wafflestudio.csereal.core.reservation.dto.ReserveTermDto
import com.wafflestudio.csereal.core.reservation.dto.SimpleReservationDto
import com.wafflestudio.csereal.core.user.service.UserService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID

interface ReservationService {
    fun reserveRoom(reserveRequest: ReserveRequest): List<ReservationDto>
    fun getRoomReservationsBetween(roomId: Long, start: LocalDateTime, end: LocalDateTime): List<SimpleReservationDto>
    fun getReservation(reservationId: Long): ReservationDto
    fun getReserveTerms(): List<ReserveTermDto>
    fun cancelSpecific(reservationId: Long)
    fun cancelRecurring(recurrenceId: UUID)
}

@Service
@Transactional
class ReservationServiceImpl(
    private val reservationRepository: ReservationRepository,
    private val roomRepository: RoomRepository,
    private val reserveTermPolicy: ReserveTermPolicy,
    private val reserveTermValidationService: ReserveTermValidationService,
    private val userService: UserService,
    private val reservationProperties: ReservationProperties
) : ReservationService {

    companion object {
        private const val ROLE_STAFF = "ROLE_STAFF"
        private const val ROLE_LABMASTER = "ROLE_LABMASTER"
        private const val ROLE_RESERVATION = "ROLE_RESERVATION"
        private const val ROLE_PROFESSOR = "ROLE_PROFESSOR"
        private const val PROFESSOR_ROOM_ID = 8L
        private val MAX_NON_STAFF_DURATION = Duration.ofHours(3)
        private val SEOUL_ZONE: ZoneId = ZoneId.of("Asia/Seoul")
        private val SUPPORTED_RESERVATION_YEARS = 1001..9998
        private val MAX_SUPPORTED_RESERVATION_TIME = LocalDateTime.of(9998, 12, 31, 23, 59, 59, 999_999_000)
    }

    override fun reserveRoom(reserveRequest: ReserveRequest): List<ReservationDto> {
        validateUniversalRequest(reserveRequest)
        val roles = authenticatedCreationRoles()

        val room = roomRepository.findRoomById(reserveRequest.roomId)
            ?: throw CserealException(ErrorCode.ROOM_NOT_FOUND)
        val user = userService.getLoginUser()

        val isStaff = ROLE_STAFF in roles
        val isLabmaster = ROLE_LABMASTER in roles
        val hasReservationRole = ROLE_RESERVATION in roles

        if (!isStaff && room.type != RoomType.SEMINAR) {
            throw CserealException(ErrorCode.ONLY_SEMINAR_RESERVABLE)
        }
        if (!isStaff && reserveRequest.roomId == PROFESSOR_ROOM_ID && ROLE_PROFESSOR !in roles) {
            throw CserealException(ErrorCode.PROFESSOR_ROOM_DENIED)
        }

        val policy = if (isStaff) {
            DerivedReservationPolicy(ReservationType.UNRESTRICTED, null)
        } else {
            deriveNonStaffPolicy(reserveRequest, isLabmaster, hasReservationRole)
        }
        validateOccurrenceBounds(reserveRequest, policy)

        val recurrenceId = UUID.randomUUID()
        val reservations = (0L until reserveRequest.recurringWeeks.toLong()).map { week ->
            val start = reserveRequest.startTime.plusWeeks(week)
            val end = reserveRequest.endTime.plusWeeks(week)

            if (policy.boundedTerm != null &&
                (start.isBefore(policy.boundedTerm.termStartTime) || end.isAfter(policy.boundedTerm.termEndTime))
            ) {
                throw CserealException(ErrorCode.INVALID_RESERVATION_PERIOD)
            }

            if (reservationRepository.findByRoomIdAndTimeOverlap(reserveRequest.roomId, start, end).isNotEmpty()) {
                val message = if (policy.type == ReservationType.REGULAR) {
                    "${week + 1}주차 해당 시간에 이미 예약이 있습니다."
                } else {
                    null
                }
                throw CserealException(ErrorCode.RESERVATION_OCCUPIED, customMsg = message)
            }

            ReservationEntity.create(
                user = user,
                room = room,
                reserveRequest = reserveRequest,
                reservationType = policy.type,
                start = start,
                end = end,
                recurrenceId = recurrenceId
            )
        }

        reservationRepository.saveAll(reservations)
        return reservations.map { ReservationDto.of(it) }
    }

    private fun deriveNonStaffPolicy(
        request: ReserveRequest,
        isLabmaster: Boolean,
        hasReservationRole: Boolean
    ): DerivedReservationPolicy {
        if (!isLabmaster && !hasReservationRole) {
            throw CserealException(ErrorCode.RESERVATION_PERMISSION_DENIED)
        }

        validateNonStaffDuration(request)
        val now = reserveTermPolicy.now()

        return when (val resolution = reserveTermValidationService.resolveTarget(request.startTime)) {
            ReserveTermResolution.Missing -> deriveMissingTermPolicy(request, now)
            is ReserveTermResolution.Invalid,
            is ReserveTermResolution.Multiple -> throw CserealException(ErrorCode.TERM_NOT_REGISTERED)
            is ReserveTermResolution.Valid -> when (reserveTermPolicy.phase(resolution.term, now)) {
                ReserveTermPhase.BEFORE_APPLICATION -> {
                    val error = if (isLabmaster) ErrorCode.TERM_NOT_OPENED else ErrorCode.LABMASTER_ONLY
                    throw CserealException(error)
                }
                ReserveTermPhase.REGULAR_APPLICATION -> {
                    if (!isLabmaster) throw CserealException(ErrorCode.LABMASTER_ONLY)
                    DerivedReservationPolicy(ReservationType.REGULAR, resolution.term)
                }
                ReserveTermPhase.GAP -> throw CserealException(ErrorCode.TERM_APPLICATION_CLOSED)
                ReserveTermPhase.TERM_ACTIVE -> deriveActiveTermPolicy(request, resolution.term, now)
            }
        }
    }

    private fun deriveMissingTermPolicy(
        request: ReserveRequest,
        now: LocalDateTime
    ): DerivedReservationPolicy {
        validateOneTimeRequest(request, reserveTermPolicy.oneTimeReservationOpenTime(request.startTime), now)
        return DerivedReservationPolicy(ReservationType.ONE_TIME, null)
    }

    private fun deriveActiveTermPolicy(
        request: ReserveRequest,
        term: com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity,
        now: LocalDateTime
    ): DerivedReservationPolicy {
        validateOneTimeRequest(
            request,
            reserveTermPolicy.activeTermOneTimeReservationOpenTime(term, request.startTime),
            now
        )
        return DerivedReservationPolicy(ReservationType.ONE_TIME, term)
    }

    private fun validateOneTimeRequest(
        request: ReserveRequest,
        opening: LocalDateTime,
        now: LocalDateTime
    ) {
        if (request.recurringWeeks != 1) {
            throw CserealException(ErrorCode.ONE_TIME_RECURRING_DENIED)
        }
        if (now.isBefore(opening)) {
            throw CserealException(ErrorCode.ONE_TIME_NOT_OPENED)
        }
    }

    private fun validateUniversalRequest(request: ReserveRequest) {
        if (request.startTime.year !in SUPPORTED_RESERVATION_YEARS ||
            request.endTime.year !in SUPPORTED_RESERVATION_YEARS
        ) {
            throw CserealException(ErrorCode.UNSUPPORTED_RESERVATION_DATE)
        }
        if (!request.agreed) {
            throw CserealException.Csereal400("Policy Not Agreed")
        }
        if (!request.startTime.isBefore(request.endTime)) {
            throw CserealException(ErrorCode.INVALID_RESERVATION_TIME)
        }
        if (request.recurringWeeks < 1) {
            throw CserealException(ErrorCode.INVALID_RECURRING_WEEKS)
        }
        if (!reserveTermPolicy.now().isBefore(request.startTime)) {
            throw CserealException(ErrorCode.PAST_RESERVATION_DENIED)
        }
    }

    private fun validateNonStaffDuration(request: ReserveRequest) {
        val startDateInSeoul = request.startTime.toInstant(ZoneOffset.UTC).atZone(SEOUL_ZONE).toLocalDate()
        val endDateInSeoul = request.endTime.toInstant(ZoneOffset.UTC).atZone(SEOUL_ZONE).toLocalDate()

        if (startDateInSeoul != endDateInSeoul ||
            Duration.between(request.startTime, request.endTime) > MAX_NON_STAFF_DURATION
        ) {
            throw CserealException(ErrorCode.RESERVATION_TIME_EXCEEDED)
        }
    }

    private fun validateOccurrenceBounds(request: ReserveRequest, policy: DerivedReservationPolicy) {
        val requested = request.recurringWeeks.toLong()
        val supported = reserveTermPolicy.maxOccurrences(request.endTime, MAX_SUPPORTED_RESERVATION_TIME)
        val policyMaximum = when (policy.type) {
            ReservationType.UNRESTRICTED -> reservationProperties.maxRecurringWeeks.toLong()
            ReservationType.REGULAR -> reserveTermPolicy.maxOccurrences(
                request.endTime,
                requireNotNull(policy.boundedTerm).termEndTime
            )
            ReservationType.ONE_TIME -> 1L
        }
        if (requested > supported) {
            throw CserealException(ErrorCode.UNSUPPORTED_RESERVATION_DATE)
        }
        if (requested > policyMaximum) {
            throw CserealException(ErrorCode.INVALID_RECURRING_WEEKS)
        }
    }

    private fun authenticatedCreationRoles(): Set<String> {
        val authentication = SecurityContextHolder.getContext().authentication
        if (
            authentication == null ||
            !authentication.isAuthenticated ||
            authentication is AnonymousAuthenticationToken
        ) {
            throw CserealException.Csereal401("Authentication required")
        }
        return authentication.authorities.mapTo(mutableSetOf()) { it.authority }
    }

    @Transactional(readOnly = true)
    override fun getRoomReservationsBetween(
        roomId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<SimpleReservationDto> =
        reservationRepository.findByRoomIdAndStartTimeBetweenOrderByStartTimeAsc(roomId, start, end)
            .map { SimpleReservationDto.of(it) }

    @Transactional(readOnly = true)
    override fun getReserveTerms(): List<ReserveTermDto> =
        reserveTermValidationService.findAllValidated().map { ReserveTermDto.of(it) }

    @Transactional(readOnly = true)
    override fun getReservation(reservationId: Long): ReservationDto {
        val reservationEntity = reservationRepository.findByIdOrNull(reservationId)
            ?: throw CserealException.Csereal404("예약을 찾을 수 없습니다.")
        return if (isCurrentUserStaff()) {
            ReservationDto.of(reservationEntity)
        } else {
            ReservationDto.forNormalUser(reservationEntity)
        }
    }

    override fun cancelSpecific(reservationId: Long) {
        val user = userService.getLoginUser()
        val reservation = reservationRepository.findByIdOrNull(reservationId)
            ?: throw CserealException.Csereal404("reservation not found")
        if (!isCurrentUserStaff() && user.id != reservation.user.id) {
            throw CserealException.Csereal403("Cannot cancel other's reservation")
        }
        reservationRepository.deleteById(reservationId)
    }

    override fun cancelRecurring(recurrenceId: UUID) {
        val user = userService.getLoginUser()
        val reservation = reservationRepository.findFirstByRecurrenceId(recurrenceId)
            ?: throw CserealException.Csereal404("reservation not found")
        if (!isCurrentUserStaff() && user.id != reservation.user.id) {
            throw CserealException.Csereal403("Cannot cancel other's reservation")
        }
        reservationRepository.deleteAllByRecurrenceId(recurrenceId)
    }

    private data class DerivedReservationPolicy(
        val type: ReservationType,
        val boundedTerm: com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity?
    )
}
