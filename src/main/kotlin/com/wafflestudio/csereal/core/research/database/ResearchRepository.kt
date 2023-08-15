package com.wafflestudio.csereal.core.research.database

import org.springframework.data.jpa.repository.JpaRepository

interface ResearchRepository : JpaRepository<ResearchEntity, Long> {
    fun findAllByPostTypeOrderByPostDetail(postType: String): List<ResearchEntity>
}