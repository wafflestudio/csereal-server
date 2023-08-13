package com.wafflestudio.csereal.core.undergraduate.database

import org.springframework.data.jpa.repository.JpaRepository

interface UndergraduateRepository : JpaRepository<UndergraduateEntity, Long> {
    fun findByPostType(postType: String) : UndergraduateEntity
}
