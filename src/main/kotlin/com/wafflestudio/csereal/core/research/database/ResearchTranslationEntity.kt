package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.search.SearchIndexed
import com.wafflestudio.csereal.common.search.SearchType
import com.wafflestudio.csereal.core.research.type.ResearchType
import com.wafflestudio.csereal.common.enums.LanguageType
import jakarta.persistence.*

@Entity(name = "research_translation")
class ResearchTranslationEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_id")
    var research: ResearchEntity,

    @Enumerated(EnumType.STRING)
    var language: LanguageType,

    var name: String,

    @Column(columnDefinition = "mediumText")
    var description: String? = null

) : BaseTimeEntity(), SearchIndexed {

    override val searchType get() =
        if (research.postType == ResearchType.CENTERS) {
            SearchType.RESEARCH_CENTER
        } else {
            SearchType.RESEARCH_GROUP
        }
    override val searchSourceId get() = research.id
}
