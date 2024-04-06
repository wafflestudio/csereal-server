package com.wafflestudio.csereal.core.about.database

import org.springframework.data.jpa.repository.JpaRepository

interface StatRepository : JpaRepository<StatEntity, Long> {
    fun findAllByYear(year: Int): List<StatEntity>
    fun findAllByYearAndDegree(year: Int, degree: Degree): List<StatEntity>
}
