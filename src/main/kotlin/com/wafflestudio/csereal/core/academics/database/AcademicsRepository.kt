package com.wafflestudio.csereal.core.academics.database

import org.springframework.data.jpa.repository.JpaRepository

interface AcademicsRepository : JpaRepository<AcademicsEntity, Long> {
    fun findByStudentTypeAndPostType(studentType: String, postType: AcademicsPostType) : AcademicsEntity
    fun findByName(name: String): AcademicsEntity
}
