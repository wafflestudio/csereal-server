package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.research.type.ResearchType
import org.springframework.data.jpa.repository.JpaRepository

interface ResearchRepository : JpaRepository<ResearchEntity, Long> {
    fun findByName(name: String): ResearchEntity?
    fun findAllByPostTypeAndLanguageOrderByName(
        postType: ResearchType,
        languageType: LanguageType
    ): List<ResearchEntity>
}
