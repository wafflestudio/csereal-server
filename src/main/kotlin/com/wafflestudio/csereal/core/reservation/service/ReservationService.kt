package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.reservation.database.*
import com.wafflestudio.csereal.core.reservation.dto.ReservationDto
import com.wafflestudio.csereal.core.reservation.dto.ReserveRequest
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import java.time.LocalDateTime
import java.util.*

interface ReservationService {
    fun reserveRoom(reserveRequest: ReserveRequest): List<ReservationDto>
    fun getRoomReservationsBetween(roomId: Long, start: LocalDateTime, end: LocalDateTime): List<ReservationDto>
    fun getReservation(reservationId: Long): ReservationDto
    fun cancelSpecific(reservationId: Long)
    fun cancelRecurring(recurrenceId: UUID)
}

@Service
@Transactional
class ReservationServiceImpl(
    private val reservationRepository: ReservationRepository,
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository
) : ReservationService {

    override fun reserveRoom(reserveRequest: ReserveRequest): List<ReservationDto> {
        var user = RequestContextHolder.getRequestAttributes()?.getAttribute(
            "loggedInUser",
            RequestAttributes.SCOPE_REQUEST
        ) as UserEntity?

        if (user == null) {
            val oidcUser = SecurityContextHolder.getContext().authentication.principal as OidcUser
            val username = oidcUser.idToken.getClaim<String>("username")

            user = userRepository.findByUsername(username) ?: throw CserealException.Csereal404("재로그인이 필요합니다.")
        }

        val room =
            roomRepository.findByIdOrNull(reserveRequest.roomId) ?: throw CserealException.Csereal404("Room Not Found")

        val reservations = mutableListOf<ReservationEntity>()

        val recurrenceId = UUID.randomUUID()

        val numberOfWeeks = reserveRequest.recurringWeeks

        for (week in 0 until numberOfWeeks) {
            val start = reserveRequest.startTime.plusWeeks(week.toLong())
            val end = reserveRequest.endTime.plusWeeks(week.toLong())

            // 중복 예약 방지
            val overlappingReservations = reservationRepository.findByRoomIdAndTimeOverlap(
                reserveRequest.roomId,
                start,
                end
            )
            if (overlappingReservations.isNotEmpty()) {
                throw CserealException.Csereal409("${week}주차 해당 시간에 이미 예약이 있습니다.")
            }

            val newReservation = ReservationEntity.create(user, room, reserveRequest, start, end, recurrenceId)
            reservations.add(newReservation)
        }

        reservationRepository.saveAll(reservations)

        return reservations.map { ReservationDto.of(it) }
    }

    @Transactional(readOnly = true)
    override fun getRoomReservationsBetween(
        roomId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<ReservationDto> {
        return reservationRepository.findByRoomIdAndStartTimeBetweenOrderByStartTimeAsc(roomId, start, end)
            .map { ReservationDto.of(it) }
    }

    @Transactional(readOnly = true)
    override fun getReservation(reservationId: Long): ReservationDto {
        val reservationEntity =
            reservationRepository.findByIdOrNull(reservationId) ?: throw CserealException.Csereal404("예약을 찾을 수 없습니다.")
        return ReservationDto.of(reservationEntity)
    }

    override fun cancelSpecific(reservationId: Long) {
        reservationRepository.deleteById(reservationId)
    }

    override fun cancelRecurring(recurrenceId: UUID) {
        reservationRepository.deleteAllByRecurrenceId(recurrenceId)
    }

}
