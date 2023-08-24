package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.reservation.database.*
import com.wafflestudio.csereal.core.reservation.dto.ReservationDto
import com.wafflestudio.csereal.core.reservation.dto.ReserveRequest
import com.wafflestudio.csereal.core.user.database.Role
import com.wafflestudio.csereal.core.user.database.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface ReservationService {
    fun reserveRoom(
        username: String,
        reserveRequest: ReserveRequest
    ): List<ReservationDto>

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

    override fun reserveRoom(
        username: String,
        reserveRequest: ReserveRequest
    ): List<ReservationDto> {
        val user = userRepository.findByUsername(username) ?: throw CserealException.Csereal404("재로그인이 필요합니다.")
        val room =
            roomRepository.findByIdOrNull(reserveRequest.roomId) ?: throw CserealException.Csereal404("Room Not Found")

        if (user.role == null) {
            throw CserealException.Csereal401("권한이 없습니다.")
        }

        val reservations = mutableListOf<ReservationEntity>()

        val recurrenceId = UUID.randomUUID()

        val numberOfWeeks = reserveRequest.recurringWeeks ?: 1

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

    override fun cancelSpecific(reservationId: Long) {
        reservationRepository.deleteById(reservationId)
    }

    override fun cancelRecurring(recurrenceId: UUID) {
        reservationRepository.deleteAllByRecurrenceId(recurrenceId)
    }

}
