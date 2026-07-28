package com.wafflestudio.csereal.core.reservation.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ReserveTermRepository : JpaRepository<ReserveTermEntity, Long> {

    @Query(
        """
        SELECT rt FROM reserve_term rt
        WHERE rt.termStartTime <= :request_start AND rt.termEndTime > :request_start
        ORDER BY rt.id ASC
        """
    )
    fun findContainingRequestStart(
        @Param("request_start") requestStart: LocalDateTime
    ): List<ReserveTermEntity>

    @Query(
        """
        SELECT rt FROM reserve_term rt
        WHERE rt.termStartTime < :end AND rt.termEndTime > :start
        ORDER BY rt.id ASC
        """
    )
    fun findByTimeOverlap(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<ReserveTermEntity>

    fun findByTermYearAndTermTypeOrderByIdAsc(
        termYear: Int,
        termType: ReserveTermType
    ): List<ReserveTermEntity>

    fun findAllByOrderByApplyStartTimeAscTermStartTimeAscIdAsc(): List<ReserveTermEntity>
}
