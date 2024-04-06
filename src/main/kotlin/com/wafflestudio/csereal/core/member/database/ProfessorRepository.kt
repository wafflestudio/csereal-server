package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.enums.LanguageType
import org.springframework.data.jpa.repository.JpaRepository

interface ProfessorRepository : JpaRepository<ProfessorEntity, Long> {
    fun findByLanguageAndStatus(
        languageType: LanguageType,
        status: ProfessorStatus
    ): List<ProfessorEntity>
    fun findByLanguageAndStatusNot(
        languageType: LanguageType,
        status: ProfessorStatus
    ): List<ProfessorEntity>
}
