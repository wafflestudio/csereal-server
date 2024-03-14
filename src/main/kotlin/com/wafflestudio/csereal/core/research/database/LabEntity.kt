package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.member.database.ProfessorEntity
import com.wafflestudio.csereal.core.research.dto.LabDto
import com.wafflestudio.csereal.core.research.dto.LabUpdateRequest
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import jakarta.persistence.*

@Entity(name = "lab")
class LabEntity(
    var language: LanguageType,
    var name: String,

    @OneToMany(mappedBy = "lab")
    val professors: MutableSet<ProfessorEntity> = mutableSetOf(),

    var location: String?,
    var tel: String?,
    var acronym: String?,

    var youtube: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_id")
    var research: ResearchEntity,

    @Column(columnDefinition = "mediumText")
    var description: String?,
    var websiteURL: String?,

    @OneToMany(mappedBy = "lab", cascade = [CascadeType.ALL])
    var attachments: MutableList<AttachmentEntity> = mutableListOf(),

    @OneToOne(mappedBy = "lab", cascade = [CascadeType.ALL], orphanRemoval = true)
    var researchSearch: ResearchSearchEntity? = null

) : BaseTimeEntity(), AttachmentContentEntityType {
    override fun bringAttachments() = attachments
    companion object {
        fun of(languageType: LanguageType, labDto: LabDto, researchGroup: ResearchEntity): LabEntity {
            return LabEntity(
                language = languageType,
                name = labDto.name,
                location = labDto.location,
                tel = labDto.tel,
                acronym = labDto.acronym,
                youtube = labDto.youtube,
                research = researchGroup,
                description = labDto.description,
                websiteURL = labDto.websiteURL
            )
        }
    }

    fun updateWithoutProfessor(labUpdateRequest: LabUpdateRequest) {
        this.name = labUpdateRequest.name
        this.location = labUpdateRequest.location
        this.tel = labUpdateRequest.tel
        this.acronym = labUpdateRequest.acronym
        this.youtube = labUpdateRequest.youtube
        this.description = labUpdateRequest.description
        this.websiteURL = labUpdateRequest.websiteURL
    }
}
