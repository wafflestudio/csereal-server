package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.enums.LanguageType
import org.springframework.data.jpa.repository.JpaRepository

interface ProfessorRepository : JpaRepository<ProfessorEntity, Long>

interface ProfessorTranslationRepository : JpaRepository<ProfessorTranslationEntity, Long> {
    fun findAllByLanguageAndProfessorStatus(
        language: LanguageType,
        status: ProfessorStatus
    ): List<ProfessorTranslationEntity>

    fun findAllByLanguageAndProfessorStatusNot(
        language: LanguageType,
        status: ProfessorStatus
    ): List<ProfessorTranslationEntity>
}
