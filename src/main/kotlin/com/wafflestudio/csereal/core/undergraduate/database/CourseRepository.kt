package com.wafflestudio.csereal.core.undergraduate.database

import org.springframework.data.jpa.repository.JpaRepository

interface CourseRepository : JpaRepository<CourseEntity, Long> {
    fun findAllByOrderByYearAsc() : List<CourseEntity>
    fun findByTitle(title: String) : CourseEntity
}