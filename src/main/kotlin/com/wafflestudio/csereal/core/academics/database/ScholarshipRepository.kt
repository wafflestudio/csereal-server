package com.wafflestudio.csereal.core.academics.database

import org.springframework.data.jpa.repository.JpaRepository

interface ScholarshipRepository : JpaRepository<ScholarshipEntity, Long> {
    fun findAllByStudentType(studentType: AcademicsStudentType): List<ScholarshipEntity>
}
