package com.wafflestudio.csereal.core.reservation.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime
import java.util.UUID

interface ReservationRepository : JpaRepository<ReservationEntity, Long> {

    @Query(
        """
        SELECT r FROM reservation r
        WHERE r.room.id = :roomId
        AND r.startTime < :end AND r.endTime > :start
        """
    )
    fun findByRoomIdAndTimeOverlap(roomId: Long, start: LocalDateTime, end: LocalDateTime): List<ReservationEntity>

    fun findByRoomIdAndStartTimeBetweenOrderByStartTimeAsc(
        roomId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<ReservationEntity>

    fun deleteAllByRecurrenceId(recurrenceId: UUID)
    fun findFirstByRecurrenceId(recurrenceId: UUID): ReservationEntity?
}
