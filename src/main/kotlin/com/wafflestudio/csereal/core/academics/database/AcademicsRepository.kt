package com.wafflestudio.csereal.core.academics.database

import org.springframework.data.jpa.repository.JpaRepository

interface AcademicsRepository : JpaRepository<AcademicsEntity, Long> {
    fun findByStudentTypeAndPostType(
        studentType: AcademicsStudentType,
        postType: AcademicsPostType
    ): AcademicsEntity
    fun findAllByStudentTypeAndPostTypeOrderByYearDesc(
        studentType: AcademicsStudentType,
        postType: AcademicsPostType
    ): List<AcademicsEntity>
    fun findAllByStudentTypeAndPostTypeOrderByTimeDesc(
        studentType: AcademicsStudentType,
        postType: AcademicsPostType
    ): List<AcademicsEntity>
}
