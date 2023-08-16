package com.wafflestudio.csereal.core.research.database

import org.springframework.data.jpa.repository.JpaRepository

interface ResearchRepository : JpaRepository<ResearchEntity, Long> {
    fun findByName(name: String): ResearchEntity?
    fun findAllByPostTypeOrderByName(postType: ResearchPostType): List<ResearchEntity>
}