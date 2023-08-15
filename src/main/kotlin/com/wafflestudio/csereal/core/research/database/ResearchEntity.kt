package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.research.dto.ResearchDto
import jakarta.persistence.*

@Entity(name = "research")
class ResearchEntity(
    @Enumerated(EnumType.STRING)
    var postType: ResearchPostType,

    var name: String,

    var description: String?,

    var websiteURL: String?,

    var isPublic: Boolean,

    @OneToMany(mappedBy = "research", cascade = [CascadeType.ALL], orphanRemoval = true)
    var labs: MutableList<LabEntity> = mutableListOf()
): BaseTimeEntity() {
    companion object {
        fun of(researchDto: ResearchDto) : ResearchEntity {
            return ResearchEntity(
                postType = researchDto.postType,
                name = researchDto.name,
                description = researchDto.description,
                websiteURL = researchDto.websiteURL,
                isPublic = researchDto.isPublic
            )
        }
    }
}