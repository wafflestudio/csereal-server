package com.wafflestudio.csereal.core.reservation.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ReserveTermRepository : JpaRepository<ReserveTermEntity, Long> {

    @Query("""
        SELECT rt FROM reserve_term rt
        WHERE rt.termStartTime <= :start AND rt.termEndTime >= :end
        """
    )
    fun findByTimeInclude(
        @Param("start") start: LocalDateTime, @Param("end") end: LocalDateTime
    ): List<ReserveTermEntity>

    @Query("""
        SELECT rt FROM reserve_term rt 
        WHERE rt.termStartTime < :end AND rt.termEndTime > :start
        """
    )
    fun findByTimeOverlap(
        @Param("start") start: LocalDateTime, @Param("end") end: LocalDateTime
    ): List<ReserveTermEntity>

    @Query("""SELECT MAX(rt.termEndTime) FROM reserve_term rt""")
    fun findLastEndTime(): LocalDateTime?
}
