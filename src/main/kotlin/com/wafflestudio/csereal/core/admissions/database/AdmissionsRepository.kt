package com.wafflestudio.csereal.core.admissions.database

import com.wafflestudio.csereal.core.admissions.database.AdmissionsEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AdmissionsRepository : JpaRepository<AdmissionsEntity, Long> {
    fun findByAdmissionsType(admissionsType: String) : AdmissionsEntity
}