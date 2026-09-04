package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.research.type.ResearchType
import org.springframework.data.jpa.repository.JpaRepository

interface ResearchRepository : JpaRepository<ResearchEntity, Long> {
    fun findByIdAndPostType(id: Long, postType: ResearchType): ResearchEntity?
}

interface ResearchTranslationRepository : JpaRepository<ResearchTranslationEntity, Long> {
    fun findAllByLanguageAndResearchPostTypeOrderByName(
        language: LanguageType,
        postType: ResearchType
    ): List<ResearchTranslationEntity>
}
