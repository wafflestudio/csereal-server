package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.research.type.ResearchRelatedType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity(name = "research_language")
class ResearchLanguageEntity(
    @Column(nullable = false, unique = true)
    val koreanId: Long,

    @Column(nullable = false, unique = true)
    val englishId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: ResearchRelatedType
) : BaseTimeEntity()
