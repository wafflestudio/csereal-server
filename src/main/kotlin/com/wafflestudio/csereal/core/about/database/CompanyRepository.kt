package com.wafflestudio.csereal.core.about.database

import org.springframework.data.jpa.repository.JpaRepository

interface CompanyRepository : JpaRepository<CompanyEntity, Long> {
    fun findAllByOrderByYearDesc(): List<CompanyEntity>
}
