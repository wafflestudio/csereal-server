package com.wafflestudio.csereal.core.member.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.database.ProfessorTranslationEntity
import com.wafflestudio.csereal.core.member.database.ProfessorStatus
import java.time.LocalDate

data class ProfessorDto(
    var id: Long,
    val language: String,
    val name: String,
    val status: ProfessorStatus,
    val academicRank: String,
    val department: String,
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
    var imageURL: String? = null
) {
    companion object {
        fun of(translation: ProfessorTranslationEntity, imageURL: String?): ProfessorDto {
            val professor = translation.professor
            return ProfessorDto(
                id = professor.id,
                language = LanguageType.makeLowercase(translation.language),
                name = translation.name,
                status = professor.status,
                academicRank = translation.academicRank,
                department = translation.department,
                labId = professor.lab?.id,
                // 연구실 이름은 이 번역본과 같은 언어판에서.
                labName = professor.lab?.translationOf(translation.language)?.name,
                startDate = professor.startDate,
                endDate = professor.endDate,
                office = translation.office,
                phone = professor.phone,
                fax = professor.fax,
                email = professor.email,
                website = professor.website,
                educations = translation.educations,
                researchAreas = translation.researchAreas,
                careers = translation.careers,
                imageURL = imageURL
            )
        }
    }
}
