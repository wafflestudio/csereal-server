package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.properties.LanguageType
import org.springframework.data.jpa.repository.JpaRepository

interface ResearchRepository : JpaRepository<ResearchEntity, Long> {
    fun findByName(name: String): ResearchEntity?
    fun findAllByPostTypeAndLanguageOrderByName(
        postType: ResearchPostType,
        languageType: LanguageType
    ): List<ResearchEntity>
}
