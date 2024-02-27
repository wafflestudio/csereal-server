package com.wafflestudio.csereal.core.member.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.member.database.ProfessorEntity
import com.wafflestudio.csereal.core.member.database.ProfessorStatus
import java.time.LocalDate

data class ProfessorDto(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var id: Long? = null,
    val language: String,
    val name: String,
    val status: ProfessorStatus,
    val academicRank: String,
    val labId: Long?,
    val labName: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val office: String?,
    val phone: String?,
    val fax: String?,
    val email: String?,
    val website: String?,
    val educations: List<String>,
    val researchAreas: List<String>,
    val careers: List<String>,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var imageURL: String? = null

) {
    companion object {
        fun of(professorEntity: ProfessorEntity, imageURL: String?): ProfessorDto {
            return ProfessorDto(
                id = professorEntity.id,
                language = LanguageType.makeLowercase(professorEntity.language),
                name = professorEntity.name,
                status = professorEntity.status,
                academicRank = professorEntity.academicRank,
                labId = professorEntity.lab?.id,
                labName = professorEntity.lab?.name,
                startDate = professorEntity.startDate,
                endDate = professorEntity.endDate,
                office = professorEntity.office,
                phone = professorEntity.phone,
                fax = professorEntity.fax,
                email = professorEntity.email,
                website = professorEntity.website,
                educations = professorEntity.educations.map { it.name },
                researchAreas = professorEntity.researchAreas.map { it.name },
                careers = professorEntity.careers.map { it.name },
                imageURL = imageURL
            )
        }
    }
}
