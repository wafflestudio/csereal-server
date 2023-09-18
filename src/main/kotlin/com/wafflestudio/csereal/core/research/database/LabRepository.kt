package com.wafflestudio.csereal.core.research.database

import org.springframework.data.jpa.repository.JpaRepository

interface LabRepository : JpaRepository<LabEntity, Long> {
    fun findAllByOrderByName(): List<LabEntity>
    fun findByName(name: String): LabEntity?
}
