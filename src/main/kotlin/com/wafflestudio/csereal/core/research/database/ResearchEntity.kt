package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.research.dto.ResearchDto
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity(name = "research")
class ResearchEntity(
    var postType: String,

    var postDetail: String?,

    var title: String,

    var description: String,

    var isPublic: Boolean,

    @OneToMany(mappedBy = "research", cascade = [CascadeType.ALL], orphanRemoval = true)
    val labs: MutableList<LabEntity> = mutableListOf()
): BaseTimeEntity() {
    companion object {
        fun of(postType: String, postDetail: String?, researchDto: ResearchDto) : ResearchEntity {
            return ResearchEntity(
                postType = postType,
                postDetail = postDetail,
                title = researchDto.title,
                description = researchDto.description,
                isPublic = researchDto.isPublic
            )
        }
    }
}