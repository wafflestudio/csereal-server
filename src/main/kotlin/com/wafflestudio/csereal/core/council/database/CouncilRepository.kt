package com.wafflestudio.csereal.core.council.database

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface CouncilRepository : JpaRepository<CouncilEntity, Long> {
    fun findAllByType(type: CouncilType, pageable: Pageable): Page<CouncilEntity>

    @Query(
        """
    SELECT c 
    FROM council c
    WHERE c.createdAt < :timestamp 
      AND c.type = 'REPORT'
    ORDER BY c.createdAt DESC
"""
    )
    fun findPreviousReport(@Param("timestamp") timestamp: LocalDateTime): CouncilEntity?

    @Query(
        """
    SELECT c 
    FROM council c
    WHERE c.createdAt > :timestamp 
      AND c.type = 'REPORT'
    ORDER BY c.createdAt ASC
"""
    )
    fun findNextReport(@Param("timestamp") timestamp: LocalDateTime): CouncilEntity?
}
