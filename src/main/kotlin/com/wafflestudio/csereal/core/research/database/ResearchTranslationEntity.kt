package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
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
    var description: String? = null,

    @OneToOne(mappedBy = "research", cascade = [CascadeType.ALL], orphanRemoval = true)
    var researchSearch: ResearchSearchEntity? = null
) : BaseTimeEntity()
