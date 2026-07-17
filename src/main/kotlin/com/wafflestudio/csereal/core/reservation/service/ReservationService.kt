package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.utils.getCurrentUserRoles
import com.wafflestudio.csereal.common.utils.isCurrentUserStaff
import com.wafflestudio.csereal.common.utils.isCurrentUserStaffOrProfessor
import com.wafflestudio.csereal.core.reservation.database.ReservationEntity
import com.wafflestudio.csereal.core.reservation.database.ReservationRepository
import com.wafflestudio.csereal.core.reservation.database.ReservationType
import com.wafflestudio.csereal.core.reservation.database.RoomRepository
import com.wafflestudio.csereal.core.reservation.database.RoomType
import com.wafflestudio.csereal.core.reservation.database.resolveRequestReservationType
import com.wafflestudio.csereal.core.reservation.dto.ReservationDto
import com.wafflestudio.csereal.core.reservation.dto.ReserveRequest
import com.wafflestudio.csereal.core.reservation.dto.ReserveTermDto
import com.wafflestudio.csereal.core.reservation.dto.SimpleReservationDto
import com.wafflestudio.csereal.core.user.service.UserService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
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
    private val userService: UserService
) : ReservationService {

    override fun reserveRoom(reserveRequest: ReserveRequest): List<ReservationDto> {
        validateUniversalRequest(reserveRequest)

        val reservationType = resolveRequestReservationType(
            reserveRequest.reservationType,
            reserveRequest.recurringWeeks
        )
        validateTypeInvariant(reservationType, reserveRequest.recurringWeeks)

        val roles = getCurrentUserRoles()
        val isStaff = "ROLE_STAFF" in roles
        val isLeader = "ROLE_LABMASTER" in roles
        validateTypePermission(reservationType, roles)

        val room = roomRepository.findRoomById(reserveRequest.roomId)
            ?: throw CserealException(ErrorCode.ROOM_NOT_FOUND)
        val user = userService.getLoginUser()

        if (!isStaff && room.type != RoomType.SEMINAR) {
            throw CserealException(ErrorCode.ONLY_SEMINAR_RESERVABLE)
        }
        if (!isCurrentUserStaffOrProfessor() && reserveRequest.roomId == 8L) {
            throw CserealException(ErrorCode.PROFESSOR_ROOM_DENIED)
        }

        val regularTerm = when (reservationType) {
            ReservationType.AD_HOC -> {
                if (
                    !isStaff &&
                    reserveTermPolicy.now().isBefore(
                        reserveTermPolicy.adHocOpenTime(reserveRequest.startTime)
                    )
                ) {
                    throw CserealException(ErrorCode.AD_HOC_NOT_OPENED)
                }
                null
            }
            ReservationType.REGULAR -> {
                if (!isStaff && !isLeader) {
                    throw CserealException(ErrorCode.LABMASTER_ONLY)
                }
                if (!isStaff) {
                    val descriptor = reserveTermPolicy.descriptorFor(reserveRequest.startTime.toLocalDate())
                    val term = reserveTermValidationService.findValidated(descriptor)
                        ?: throw CserealException(ErrorCode.TERM_NOT_REGISTERED)
                    val now = reserveTermPolicy.now()
                    if (now.isBefore(term.applyStartTime)) {
                        throw CserealException(ErrorCode.TERM_NOT_OPENED)
                    }
                    if (!now.isBefore(term.applyEndTime)) {
                        throw CserealException(ErrorCode.TERM_APPLICATION_CLOSED)
                    }
                    if (reserveRequest.startTime.plusHours(3).isBefore(reserveRequest.endTime)) {
                        throw CserealException(ErrorCode.RESERVATION_TIME_EXCEEDED)
                    }
                    term
                } else {
                    null
                }
            }
        }

        val requestedOccurrences = reserveRequest.recurringWeeks.toLong()
        val supportedDateOccurrences = reserveTermPolicy.maxOccurrences(
            reserveRequest.endTime,
            MAX_SUPPORTED_RESERVATION_TIME
        )
        if (requestedOccurrences > supportedDateOccurrences) {
            throw CserealException(ErrorCode.UNSUPPORTED_RESERVATION_DATE)
        }
        val maxOccurrences = if (regularTerm != null) {
            reserveTermPolicy.maxOccurrences(reserveRequest.endTime, regularTerm.termEndTime)
        } else if (reservationType == ReservationType.REGULAR) {
            reserveTermPolicy.staffMaxOccurrences()
        } else {
            1L
        }
        if (requestedOccurrences > maxOccurrences) {
            throw CserealException(ErrorCode.INVALID_RECURRING_WEEKS)
        }

        val recurrenceId = UUID.randomUUID()
        val reservations = (0L until requestedOccurrences).map { week ->
            val start = reserveRequest.startTime.plusWeeks(week)
            val end = reserveRequest.endTime.plusWeeks(week)

            if (regularTerm != null &&
                (start.isBefore(regularTerm.termStartTime) || end.isAfter(regularTerm.termEndTime))
            ) {
                throw CserealException(ErrorCode.INVALID_RESERVATION_PERIOD)
            }

            val overlappingReservations = reservationRepository.findByRoomIdAndTimeOverlap(
                reserveRequest.roomId,
                start,
                end
            )
            if (overlappingReservations.isNotEmpty()) {
                if (reservationType == ReservationType.REGULAR) {
                    throw CserealException(
                        ErrorCode.RESERVATION_OCCUPIED,
                        customMsg = "${week + 1}주차 해당 시간에 이미 예약이 있습니다."
                    )
                }
                throw CserealException(ErrorCode.RESERVATION_OCCUPIED)
            }

            ReservationEntity.create(
                user = user,
                room = room,
                reserveRequest = reserveRequest,
                reservationType = reservationType,
                start = start,
                end = end,
                recurrenceId = recurrenceId
            )
        }

        reservationRepository.saveAll(reservations)
        return reservations.map { ReservationDto.of(it) }
    }

    private fun validateUniversalRequest(reserveRequest: ReserveRequest) {
        if (
            reserveRequest.startTime.year !in SUPPORTED_RESERVATION_YEARS ||
            reserveRequest.endTime.year !in SUPPORTED_RESERVATION_YEARS
        ) {
            throw CserealException(ErrorCode.UNSUPPORTED_RESERVATION_DATE)
        }
        if (!reserveRequest.agreed) {
            throw CserealException.Csereal400("Policy Not Agreed")
        }
        if (!reserveRequest.startTime.isBefore(reserveRequest.endTime)) {
            throw CserealException(ErrorCode.INVALID_RESERVATION_TIME)
        }
        if (reserveRequest.recurringWeeks < 1) {
            throw CserealException(ErrorCode.INVALID_RECURRING_WEEKS)
        }
        if (!reserveTermPolicy.now().isBefore(reserveRequest.startTime)) {
            throw CserealException(ErrorCode.PAST_RESERVATION_DENIED)
        }
    }

    private fun validateTypeInvariant(reservationType: ReservationType, recurringWeeks: Int) {
        if (reservationType == ReservationType.AD_HOC && recurringWeeks != 1) {
            throw CserealException(ErrorCode.AD_HOC_RECURRING_DENIED)
        }
    }

    private fun validateTypePermission(reservationType: ReservationType, roles: List<String>) {
        val isStaff = "ROLE_STAFF" in roles
        val isLeader = "ROLE_LABMASTER" in roles
        val hasReservationRole = "ROLE_RESERVATION" in roles
        val allowed = when (reservationType) {
            ReservationType.AD_HOC -> isStaff || isLeader || hasReservationRole
            ReservationType.REGULAR -> isStaff || isLeader
        }
        if (!allowed) {
            throw CserealException(
                if (reservationType == ReservationType.REGULAR) {
                    ErrorCode.LABMASTER_ONLY
                } else {
                    ErrorCode.RESERVATION_PERMISSION_DENIED
                }
            )
        }
    }

    @Transactional(readOnly = true)
    override fun getRoomReservationsBetween(
        roomId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<SimpleReservationDto> {
        return reservationRepository.findByRoomIdAndStartTimeBetweenOrderByStartTimeAsc(roomId, start, end)
            .map { SimpleReservationDto.of(it) }
    }

    @Transactional(readOnly = true)
    override fun getReserveTerms(): List<ReserveTermDto> {
        return reserveTermValidationService.findAllValidated().map { ReserveTermDto.of(it) }
    }

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

    companion object {
        private val SUPPORTED_RESERVATION_YEARS = 1001..9998
        private val MAX_SUPPORTED_RESERVATION_TIME =
            LocalDateTime.of(9998, 12, 31, 23, 59, 59, 999_999_000)
    }
}
