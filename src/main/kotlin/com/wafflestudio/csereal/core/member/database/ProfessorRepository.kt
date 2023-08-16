package com.wafflestudio.csereal.core.member.database

import org.springframework.data.jpa.repository.JpaRepository

interface ProfessorRepository : JpaRepository<ProfessorEntity, Long> {
    fun findByStatus(status: ProfessorStatus): List<ProfessorEntity>
    fun findByStatusNot(status: ProfessorStatus): List<ProfessorEntity>
}
