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
      AND c.type = :type
    ORDER BY c.createdAt DESC
"""
    )
    fun findPreviousByType(
        @Param("timestamp") timestamp: LocalDateTime,
        @Param("type") type: CouncilType
    ): CouncilEntity?

    @Query(
        """
    SELECT c 
    FROM council c
    WHERE c.createdAt > :timestamp 
      AND c.type = :type
    ORDER BY c.createdAt ASC
"""
    )
    fun findNextByType(
        @Param("timestamp") timestamp: LocalDateTime,
        @Param("type") type: CouncilType
    ): CouncilEntity?
}
