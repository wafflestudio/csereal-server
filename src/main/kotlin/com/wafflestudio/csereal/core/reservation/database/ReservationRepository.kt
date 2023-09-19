package com.wafflestudio.csereal.core.reservation.database

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime
import java.util.UUID

interface ReservationRepository : JpaRepository<ReservationEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        "SELECT r FROM reservation r WHERE r.room.id = :roomId AND ((:start <= r.startTime AND r.startTime < :end) OR (:start < r.endTime AND r.endTime <= :end) OR (r.startTime <= :start AND r.endTime >= :end))"
    )
    fun findByRoomIdAndTimeOverlap(roomId: Long, start: LocalDateTime, end: LocalDateTime): List<ReservationEntity>

    fun findByRoomIdAndStartTimeBetweenOrderByStartTimeAsc(
        roomId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<ReservationEntity>

    fun deleteAllByRecurrenceId(recurrenceId: UUID)
}
