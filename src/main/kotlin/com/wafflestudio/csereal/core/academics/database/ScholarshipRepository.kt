package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.enums.LanguageType
import org.springframework.data.jpa.repository.JpaRepository

interface ScholarshipRepository : JpaRepository<ScholarshipEntity, Long>

interface ScholarshipTranslationRepository : JpaRepository<ScholarshipTranslationEntity, Long> {
    fun findAllByScholarshipStudentTypeAndLanguage(
        studentType: AcademicsStudentType,
        language: LanguageType
    ): List<ScholarshipTranslationEntity>
}
