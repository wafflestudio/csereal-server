package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.core.member.database.ProfessorEntity
import com.wafflestudio.csereal.core.research.dto.LabDto
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import jakarta.persistence.*

@Entity(name = "lab")
class LabEntity(
    val name: String,

    @OneToMany(mappedBy = "lab")
    val professors: MutableSet<ProfessorEntity> = mutableSetOf(),

    val location: String?,
    val tel: String?,
    val acronym: String?,
    val pdf: String?,
    val youtube: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_id")
    var research: ResearchEntity,

    val description: String?,
    val websiteURL: String?,

    @OneToMany(mappedBy = "lab", cascade = [CascadeType.ALL], orphanRemoval = true)
    var attachments: MutableList<AttachmentEntity> = mutableListOf(),

) : BaseTimeEntity(), AttachmentContentEntityType {
    override fun bringAttachments() = attachments
    companion object {
        fun of(researchGroup: ResearchEntity, labDto: LabDto) : LabEntity {
            return LabEntity(
                name = labDto.name,
                location = labDto.location,
                tel = labDto.tel,
                acronym = labDto.acronym,
                pdf = labDto.introductionMaterials?.pdf,
                youtube = labDto.introductionMaterials?.youtube,
                research = researchGroup,
                description = labDto.description,
                websiteURL = labDto.websiteURL,
            )
        }
    }
}
