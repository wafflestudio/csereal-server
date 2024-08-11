package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.enums.LanguageType
import org.springframework.data.jpa.repository.JpaRepository

interface AcademicsRepository : JpaRepository<AcademicsEntity, Long> {
    fun findByLanguageAndStudentTypeAndPostType(
        languageType: LanguageType,
        studentType: AcademicsStudentType,
        postType: AcademicsPostType
    ): AcademicsEntity?

    fun findByLanguageAndStudentTypeAndPostTypeAndYear(
        languageType: LanguageType,
        studentType: AcademicsStudentType,
        postType: AcademicsPostType,
        year: Int?
    ): AcademicsEntity?

    fun findAllByLanguageAndStudentTypeAndPostTypeOrderByYearDesc(
        languageType: LanguageType,
        studentType: AcademicsStudentType,
        postType: AcademicsPostType
    ): List<AcademicsEntity>
}
