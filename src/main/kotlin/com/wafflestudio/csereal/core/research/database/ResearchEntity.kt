package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.entity.MainImageAttachable
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.research.type.ResearchType
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import jakarta.persistence.*

// 연구그룹·연구센터 자체. 대표이미지·소속 연구실처럼 언어와 무관한 것만 든다.
@Entity(name = "research")
class ResearchEntity(
    @Enumerated(EnumType.STRING)
    val postType: ResearchType,

    var websiteURL: String? = null,

    @OneToMany(mappedBy = "research", cascade = [CascadeType.PERSIST])
    var labs: MutableSet<LabEntity> = mutableSetOf(),

    @OneToOne
    override var mainImage: MainImageEntity? = null,

    @OneToMany(mappedBy = "research", cascade = [CascadeType.ALL], orphanRemoval = true)
    var translations: MutableList<ResearchTranslationEntity> = mutableListOf()
) : BaseTimeEntity(), MainImageAttachable {
    fun translationOf(language: LanguageType): ResearchTranslationEntity? =
        translations.firstOrNull { it.language == language }
}
