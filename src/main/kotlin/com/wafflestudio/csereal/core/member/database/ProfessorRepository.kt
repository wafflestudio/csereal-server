package com.wafflestudio.csereal.core.member.database

import org.springframework.data.jpa.repository.JpaRepository

interface ProfessorRepository : JpaRepository<ProfessorEntity, Long> {
    fun findByIsActiveTrue(): List<ProfessorEntity>
    fun findByIsActiveFalse(): List<ProfessorEntity>
}
