package com.wafflestudio.csereal.core.academics.database

import org.springframework.data.jpa.repository.JpaRepository

interface CourseRepository : JpaRepository<CourseEntity, Long> {
    fun findAllByStudentTypeOrderByYearAsc(studentType: String) : List<CourseEntity>
    fun findByName(name: String) : CourseEntity
}