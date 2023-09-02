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

    @OneToOne
    var pdf: AttachmentEntity? = null,

    val youtube: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_id")
    var research: ResearchEntity,

    val description: String?,
    val websiteURL: String?,

) : BaseTimeEntity() {
    companion object {
        fun of(labDto: LabDto, researchGroup: ResearchEntity) : LabEntity {
            return LabEntity(
                name = labDto.name,
                location = labDto.location,
                tel = labDto.tel,
                acronym = labDto.acronym,
                youtube = labDto.youtube,
                research = researchGroup,
                description = labDto.description,
                websiteURL = labDto.websiteURL,
            )
        }
    }
}
