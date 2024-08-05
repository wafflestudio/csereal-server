package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.enums.LanguageType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CourseRepository : JpaRepository<CourseEntity, Long> {
    interface CourseProjection {
        val code: String
        val credit: Int
        val grade: Int
        val koName: String
        val koDescription: String
        val koClassification: String
        val enName: String
        val enDescription: String
        val enClassification: String
    }

    fun findAllByLanguageAndStudentTypeOrderByNameAsc(
        languageType: LanguageType,
        studentType: AcademicsStudentType
    ): List<CourseEntity>

    @Query(
        """
        SELECT 
            c.code as code,
            MAX(c.credit) as credit,
            MAX(c.grade) as grade,
            MAX(CASE WHEN c.language = 'KO' THEN c.name ELSE '' END) as koName,
            MAX(CASE WHEN c.language = 'KO' THEN c.description ELSE '' END) as koDescription,
            MAX(CASE WHEN c.language = 'KO' THEN c.classification ELSE '' END) as koClassification,
            MAX(CASE WHEN c.language = 'EN' THEN c.name ELSE '' END) as enName,
            MAX(CASE WHEN c.language = 'EN' THEN c.description ELSE '' END) as enDescription,
            MAX(CASE WHEN c.language = 'EN' THEN c.classification ELSE '' END) as enClassification
        FROM course c
        WHERE c.studentType = :studentType
        GROUP BY c.code
    """
    )
    fun findGroupedCourses(@Param("studentType") studentType: AcademicsStudentType): List<CourseProjection>

    fun existsByCode(code: String): Boolean
}
