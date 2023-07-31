package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.member.dto.ProfessorDto
import com.wafflestudio.csereal.core.research.database.LabEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import java.time.LocalDate

@Entity(name = "professor")
class ProfessorEntity(
    val name: String,
    //val profileImage:File
    var isActive: Boolean,
    var academicRank: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id")
    var lab: LabEntity? = null,

    var startDate: LocalDate?,
    var endDate: LocalDate?,
    val office: String?,
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

    ) : BaseTimeEntity() {

    companion object {
        fun of(professorDto: ProfessorDto): ProfessorEntity {
            return ProfessorEntity(
                name = professorDto.name,
                isActive = professorDto.isActive,
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
        this.lab = lab
        lab.professors.add(this)
    }
}
