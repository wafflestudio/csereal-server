package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.enums.LanguageType
import org.springframework.data.jpa.repository.JpaRepository

interface CourseRepository : JpaRepository<CourseEntity, Long> {
    fun findAllByLanguageAndStudentTypeOrderByNameAsc(
        languageType: LanguageType,
        studentType: AcademicsStudentType
    ): List<CourseEntity>
    fun findByLanguageAndName(
        languageType: LanguageType,
        name: String
    ): CourseEntity
}
