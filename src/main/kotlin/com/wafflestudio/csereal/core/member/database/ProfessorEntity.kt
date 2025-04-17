package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.MainImageContentEntityType
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.utils.StringListConverter
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

    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter::class)
    var educations: MutableList<String> = mutableListOf(),

    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter::class)
    var researchAreas: MutableList<String> = mutableListOf(),

    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter::class)
    var careers: MutableList<String> = mutableListOf(),

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
}

enum class ProfessorStatus(
    val krValue: String
) {
    ACTIVE("교수"),
    INACTIVE("역대 교수"),
    VISITING("객원교수");
}
