package com.wafflestudio.csereal.core.academics.database

import org.springframework.data.jpa.repository.JpaRepository

interface AcademicsRepository : JpaRepository<AcademicsEntity, Long> {
    fun findByStudentTypeAndPostType(studentType: StudentType, postType: String) : AcademicsEntity
    fun findByName(name: String): AcademicsEntity
}
