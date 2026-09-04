package com.wafflestudio.csereal.core.member.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.database.ProfessorEntity
import com.wafflestudio.csereal.core.member.database.ProfessorStatus
import com.wafflestudio.csereal.core.member.database.ProfessorTranslationEntity
import java.time.LocalDate

// 요청 본문과 같은 모양. 공유값은 최상위, 언어별 값만 ko/en 안에.
data class ProfessorLanguagesDto(
    val id: Long,
    val status: ProfessorStatus,
    val labId: Long?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val phone: String?,
    val fax: String?,
    val email: String?,
    val website: String?,
    val imageURL: String?,
    val ko: ProfessorTranslationDto?,
    val en: ProfessorTranslationDto?
) {
    companion object {
        fun of(professor: ProfessorEntity, imageURL: String?) = ProfessorLanguagesDto(
            id = professor.id,
            status = professor.status,
            labId = professor.lab?.id,
            startDate = professor.startDate,
            endDate = professor.endDate,
            phone = professor.phone,
            fax = professor.fax,
            email = professor.email,
            website = professor.website,
            imageURL = imageURL,
            ko = professor.translationOf(LanguageType.KO)?.let { ProfessorTranslationDto.of(it) },
            en = professor.translationOf(LanguageType.EN)?.let { ProfessorTranslationDto.of(it) }
        )
    }
}

data class ProfessorTranslationDto(
    val name: String,
    val academicRank: String,
    val department: String,
    // 호실 표기와 소속 연구실 이름은 언어판마다 다르다.
    val office: String?,
    val labName: String?,
    val educations: List<String>,
    val researchAreas: List<String>,
    val careers: List<String>
) {
    companion object {
        fun of(translation: ProfessorTranslationEntity) = ProfessorTranslationDto(
            name = translation.name,
            academicRank = translation.academicRank,
            department = translation.department,
            office = translation.office,
            labName = translation.professor.lab?.translationOf(translation.language)?.name,
            educations = translation.educations,
            researchAreas = translation.researchAreas,
            careers = translation.careers
        )
    }
}
