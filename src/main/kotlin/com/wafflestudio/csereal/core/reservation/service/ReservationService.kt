package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.reservation.database.*
import com.wafflestudio.csereal.core.reservation.dto.ReservationDto
import com.wafflestudio.csereal.core.reservation.dto.ReserveRequest
import com.wafflestudio.csereal.core.reservation.dto.SimpleReservationDto
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import java.time.LocalDateTime
import java.util.*

interface ReservationService {
    fun reserveRoom(reserveRequest: ReserveRequest): List<ReservationDto>
    fun getRoomReservationsBetween(roomId: Long, start: LocalDateTime, end: LocalDateTime): List<SimpleReservationDto>
    fun getReservation(reservationId: Long, isStaff: Boolean): ReservationDto
    fun cancelSpecific(reservationId: Long)
    fun cancelRecurring(recurrenceId: UUID)
}

@Service
@Transactional
class ReservationServiceImpl(
    private val reservationRepository: ReservationRepository,
    private val roomRepository: RoomRepository,
    private val userRepository: UserRepository
) : ReservationService {

    override fun reserveRoom(reserveRequest: ReserveRequest): List<ReservationDto> {
        val user = RequestContextHolder.getRequestAttributes()?.getAttribute(
            "loggedInUser",
            RequestAttributes.SCOPE_REQUEST
        ) as UserEntity? ?: userRepository.findByUsername("devUser")!!

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
    ): List<SimpleReservationDto> {
        return reservationRepository.findByRoomIdAndStartTimeBetweenOrderByStartTimeAsc(roomId, start, end)
            .map { SimpleReservationDto.of(it) }
    }

    @Transactional(readOnly = true)
    override fun getReservation(reservationId: Long, isStaff: Boolean): ReservationDto {
        val reservationEntity =
            reservationRepository.findByIdOrNull(reservationId) ?: throw CserealException.Csereal404("예약을 찾을 수 없습니다.")

        return if (isStaff) {
            ReservationDto.of(reservationEntity)
        } else {
            ReservationDto.forNormalUser(reservationEntity)
        }
    }

    override fun cancelSpecific(reservationId: Long) {
        reservationRepository.deleteById(reservationId)
    }

    override fun cancelRecurring(recurrenceId: UUID) {
        reservationRepository.deleteAllByRecurrenceId(recurrenceId)
    }
}
