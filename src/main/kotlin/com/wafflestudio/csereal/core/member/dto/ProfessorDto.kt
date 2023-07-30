package com.wafflestudio.csereal.core.member.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.wafflestudio.csereal.core.member.database.ProfessorEntity
import java.time.LocalDate

data class ProfessorDto(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var id: Long? = null,
    val name: String,
    val isActive: Boolean,
    val academicRank: String,
    val labId: Long?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val office: String?,
    val phone: String?,
    val fax: String?,
    val email: String?,
    val website: String?,
    val educations: List<EducationDto>,
    val researchAreas: List<String>,
    val careers: List<CareerDto>
) {
    companion object {
        fun of(professorEntity: ProfessorEntity): ProfessorDto {
            return ProfessorDto(
                id = professorEntity.id,
                name = professorEntity.name,
                isActive = professorEntity.isActive,
                academicRank = professorEntity.academicRank,
                labId = professorEntity.lab?.id,
                startDate = professorEntity.startDate,
                endDate = professorEntity.endDate,
                office = professorEntity.office,
                phone = professorEntity.phone,
                fax = professorEntity.fax,
                email = professorEntity.email,
                website = professorEntity.website,
                educations = professorEntity.educations.map { EducationDto.of(it) },
                researchAreas = professorEntity.researchAreas.map { it.name },
                careers = professorEntity.careers.map { CareerDto.of(it) }
            )
        }
    }
}
