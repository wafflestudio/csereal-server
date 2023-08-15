package com.wafflestudio.csereal.core.admissions.database

import org.springframework.data.jpa.repository.JpaRepository

interface AdmissionsRepository : JpaRepository<AdmissionsEntity, Long> {
    fun findByToAndPostType(to: String, postType: String): AdmissionsEntity
    fun findByPostType(postType: String) : AdmissionsEntity
}