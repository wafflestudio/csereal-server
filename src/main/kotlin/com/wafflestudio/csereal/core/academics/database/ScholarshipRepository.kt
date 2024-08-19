package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.enums.LanguageType
import org.springframework.data.jpa.repository.JpaRepository

interface ScholarshipRepository : JpaRepository<ScholarshipEntity, Long> {
    fun findAllByStudentTypeAndLanguage(
        studentType: AcademicsStudentType,
        languageType: LanguageType
    ): List<ScholarshipEntity>
}
