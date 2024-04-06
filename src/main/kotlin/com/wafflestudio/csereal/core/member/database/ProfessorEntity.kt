package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.MainImageContentEntityType
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.dto.ProfessorDto
import com.wafflestudio.csereal.core.research.database.LabEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity(name = "professor")
class ProfessorEntity(
    @Enumerated(EnumType.STRING)
    var language: LanguageType,

    var name: String,

    @Enumerated(EnumType.STRING)
    var status: ProfessorStatus,

    var academicRank: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id")
    var lab: LabEntity? = null,

    var startDate: LocalDate?,
    var endDate: LocalDate?,

    var office: String?,
    var phone: String?,
    var fax: String?,
    var email: String?,
    var website: String?,

    @OneToMany(mappedBy = "professor", cascade = [CascadeType.ALL], orphanRemoval = true)
    val educations: MutableList<EducationEntity> = mutableListOf(),

    @OneToMany(mappedBy = "professor", cascade = [CascadeType.ALL], orphanRemoval = true)
    val researchAreas: MutableList<ResearchAreaEntity> = mutableListOf(),

    @OneToMany(mappedBy = "professor", cascade = [CascadeType.ALL], orphanRemoval = true)
    val careers: MutableList<CareerEntity> = mutableListOf(),

    @OneToOne
    var mainImage: MainImageEntity? = null,

    @OneToOne(mappedBy = "professor", cascade = [CascadeType.ALL], orphanRemoval = true)
    var memberSearch: MemberSearchEntity? = null
) : BaseTimeEntity(), MainImageContentEntityType {
    override fun bringMainImage(): MainImageEntity? = mainImage

    companion object {
        fun of(languageType: LanguageType, professorDto: ProfessorDto): ProfessorEntity {
            return ProfessorEntity(
                language = languageType,
                name = professorDto.name,
                status = professorDto.status,
                academicRank = professorDto.academicRank,
                startDate = professorDto.startDate,
                endDate = professorDto.endDate,
                office = professorDto.office,
                phone = professorDto.phone,
                fax = professorDto.fax,
                email = professorDto.email,
                website = professorDto.website
            )
        }
    }

    fun addLab(lab: LabEntity) {
        this.lab?.professors?.remove(this)
        this.lab = lab
        lab.professors.add(this)
    }

    fun update(updateProfessorRequest: ProfessorDto) {
        this.name = updateProfessorRequest.name
        this.status = updateProfessorRequest.status
        this.academicRank = updateProfessorRequest.academicRank
        this.startDate = updateProfessorRequest.startDate
        this.endDate = updateProfessorRequest.endDate
        this.office = updateProfessorRequest.office
        this.phone = updateProfessorRequest.phone
        this.fax = updateProfessorRequest.fax
        this.email = updateProfessorRequest.email
        this.website = updateProfessorRequest.website
    }
}

enum class ProfessorStatus(
    val krValue: String
) {
    ACTIVE("교수"),
    INACTIVE("역대 교수"),
    VISITING("객원교수");
}
