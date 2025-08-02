package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.database.ProfessorEntity
import com.wafflestudio.csereal.core.research.dto.LabUpdateRequest
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import jakarta.persistence.*

@Entity(name = "lab")
class LabEntity(
    var language: LanguageType,

    var name: String,

    @Column(columnDefinition = "mediumText")
    var description: String?,

    var acronym: String?,

    var location: String?,

    var websiteURL: String?,

    var tel: String?,

    var youtube: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_id")
    var research: ResearchEntity? = null,

    @OneToOne
    var pdf: AttachmentEntity? = null,

    @OneToMany(mappedBy = "lab")
    var professors: MutableSet<ProfessorEntity> = mutableSetOf(),

    @OneToOne(mappedBy = "lab", cascade = [CascadeType.ALL], orphanRemoval = true)
    var researchSearch: ResearchSearchEntity? = null

) : BaseTimeEntity() {
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
