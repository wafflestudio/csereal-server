package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.common.controller.MainImageContentEntityType
import com.wafflestudio.csereal.core.research.dto.ResearchDto
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import jakarta.persistence.*

@Entity(name = "research")
class ResearchEntity(
    @Enumerated(EnumType.STRING)
    var postType: ResearchPostType,

    var name: String,

    @Column(columnDefinition = "mediumText")
    var description: String?,

    @OneToMany(mappedBy = "research", cascade = [CascadeType.ALL], orphanRemoval = true)
    var labs: MutableList<LabEntity> = mutableListOf(),

    @OneToOne
    var mainImage: MainImageEntity? = null,

    @OneToMany(mappedBy = "research", cascade = [CascadeType.ALL], orphanRemoval = true)
    var attachments: MutableList<AttachmentEntity> = mutableListOf(),

    @OneToOne(mappedBy = "research", cascade = [CascadeType.ALL], orphanRemoval = true)
    var researchSearch: ResearchSearchEntity? = null
) : BaseTimeEntity(), MainImageContentEntityType, AttachmentContentEntityType {
    override fun bringMainImage() = mainImage
    override fun bringAttachments() = attachments

    companion object {
        fun of(researchDto: ResearchDto): ResearchEntity {
            return ResearchEntity(
                postType = researchDto.postType,
                name = researchDto.name,
                description = researchDto.description
            )
        }
    }

    fun updateWithoutLabImageAttachment(researchDto: ResearchDto) {
        this.postType = researchDto.postType
        this.name = researchDto.name
        this.description = researchDto.description
    }
}
