package com.wafflestudio.csereal.core.undergraduate.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

interface UndergraduateRepository : JpaRepository<UndergraduateEntity, Long> {
    fun findByPostType(postType: String) : UndergraduateEntity?
}
