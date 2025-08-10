package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.domain.MainImageAttachable
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.research.dto.ResearchDto
import com.wafflestudio.csereal.core.research.type.ResearchType
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import jakarta.persistence.*

@Entity(name = "research")
class ResearchEntity(
    @Enumerated(EnumType.STRING)
    val postType: ResearchType,

    @Enumerated(EnumType.STRING)
    val language: LanguageType,

    var name: String,

    @Column(columnDefinition = "mediumText")
    var description: String?,

    var websiteURL: String? = null,

    @OneToMany(mappedBy = "research", cascade = [CascadeType.PERSIST])
    var labs: MutableSet<LabEntity> = mutableSetOf(),

    @OneToOne
    override var mainImage: MainImageEntity? = null,

    @OneToOne(mappedBy = "research", cascade = [CascadeType.ALL], orphanRemoval = true)
    var researchSearch: ResearchSearchEntity? = null
) : BaseTimeEntity(), MainImageAttachable {

    companion object {
        fun of(languageType: LanguageType, researchDto: ResearchDto): ResearchEntity {
            return ResearchEntity(
                postType = researchDto.postType,
                language = languageType,
                name = researchDto.name,
                description = researchDto.description,
                websiteURL = researchDto.websiteURL
            )
        }
    }
}
