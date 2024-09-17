package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.research.type.ResearchRelatedType
import jakarta.persistence.*

@Entity(name = "research_language")
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["korean_id", "english_id", "type"]),
    ]
)
class ResearchLanguageEntity(
    @Column(nullable = false)
    val koreanId: Long,

    @Column(nullable = false)
    val englishId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: ResearchRelatedType
) : BaseTimeEntity()
