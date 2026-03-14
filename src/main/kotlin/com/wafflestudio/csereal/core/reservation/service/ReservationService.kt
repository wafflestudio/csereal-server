package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.utils.isCurrentUserLeader
import com.wafflestudio.csereal.common.utils.isCurrentUserStaff
import com.wafflestudio.csereal.common.utils.isCurrentUserStaffOrProfessor
import com.wafflestudio.csereal.core.reservation.database.*
import com.wafflestudio.csereal.core.reservation.dto.ReservationDto
import com.wafflestudio.csereal.core.reservation.dto.ReserveRequest
import com.wafflestudio.csereal.core.reservation.dto.SimpleReservationDto
import com.wafflestudio.csereal.core.user.service.UserService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

interface ReservationService {
    fun reserveRoom(reserveRequest: ReserveRequest): List<ReservationDto>
    fun getRoomReservationsBetween(roomId: Long, start: LocalDateTime, end: LocalDateTime): List<SimpleReservationDto>
    fun getReservation(reservationId: Long): ReservationDto
    fun cancelSpecific(reservationId: Long)
    fun cancelRecurring(recurrenceId: UUID)
}

@Service
@Transactional
class ReservationServiceImpl(
    private val reservationRepository: ReservationRepository,
    private val roomRepository: RoomRepository,
    private val reserveTermRepository: ReserveTermRepository,
    private val userService: UserService
) : ReservationService {

    override fun reserveRoom(reserveRequest: ReserveRequest): List<ReservationDto> {
        if (!reserveRequest.agreed) {
            throw CserealException.Csereal400("Policy Not Agreed")
        }

        val user = userService.getLoginUser()

        val room =
            roomRepository.findRoomById(reserveRequest.roomId) ?: throw CserealException(ErrorCode.ROOM_NOT_FOUND)

        // 현재 일반 예약 권한으로 교수회의실 제외한 세미나실만 예약 가능 (행정실 요청)
        if (!isCurrentUserStaff() && room.type != RoomType.SEMINAR) {
            throw CserealException(ErrorCode.ONLY_SEMINAR_RESERVABLE)
        }

        // 세미나실 중 교수회의실은 스태프 또는 교수만 예약 가능
        if (!isCurrentUserStaffOrProfessor() && reserveRequest.roomId == 8L) {
            throw CserealException(ErrorCode.PROFESSOR_ROOM_DENIED)
        }

        // 세미나실은 정기예약 기간에는 랩 대표만 예약 가능
        if (!isCurrentUserStaff() && room.type == RoomType.SEMINAR) {
            val currentTime = LocalDateTime.now()
            val applyingTerms = reserveTermRepository.findByApplyTimeInclude(currentTime)
            val totalStart = reserveRequest.startTime
            val totalEnd = reserveRequest.endTime.plusWeeks(reserveRequest.recurringWeeks.toLong() - 1)

            if (!applyingTerms.isEmpty()) {
                if (!isCurrentUserLeader()) {
                    throw CserealException(ErrorCode.LABMASTER_ONLY)
                }
                if (applyingTerms.first().termStartTime > totalStart || applyingTerms.first().termEndTime < totalEnd) {
                    throw CserealException(ErrorCode.INVALID_RESERVATION_PERIOD)
                }
                if (reserveRequest.startTime.plusHours(3) < reserveRequest.endTime) {
                    throw CserealException(ErrorCode.RESERVATION_TIME_EXCEEDED)
                }
            } else {
                val lastTermEnd = reserveTermRepository.findLastEndTime()
                if (lastTermEnd != null && lastTermEnd < totalEnd) {
                    throw CserealException(ErrorCode.TERM_NOT_REGISTERED)
                }

                val overlappingTerms = reserveTermRepository.findByTimeOverlap(totalStart, totalEnd)
                overlappingTerms.forEach {
                    if (it.applyEndTime >= currentTime) {
                        throw CserealException(ErrorCode.TERM_NOT_OPENED)
                    }
                }
            }
        }

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
                throw CserealException(ErrorCode.RESERVATION_OCCUPIED, customMsg = "${week}주차 해당 시간에 이미 예약이 있습니다.")
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
    override fun getReservation(reservationId: Long): ReservationDto {
        val reservationEntity =
            reservationRepository.findByIdOrNull(reservationId) ?: throw CserealException.Csereal404("예약을 찾을 수 없습니다.")

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
}
