package com.wafflestudio.csereal.core.research.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.research.database.LabEntity
import com.wafflestudio.csereal.core.research.database.LabTranslationEntity
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse

// 요청 본문과 같은 모양. 소속·연락처·PDF 는 최상위, 언어별 값만 ko/en 안에.
data class LabLanguageDto(
    val id: Long,
    val groupId: Long?,
    val professorIds: List<Long>,
    val acronym: String?,
    val tel: String?,
    val youtube: String?,
    val websiteURL: String?,
    val pdf: AttachmentResponse?,
    val ko: LabTranslationDto?,
    val en: LabTranslationDto?
) {
    companion object {
        fun of(lab: LabEntity, pdf: AttachmentResponse?) = LabLanguageDto(
            id = lab.id,
            groupId = lab.research?.id,
            professorIds = lab.professors.map { it.id },
            acronym = lab.acronym,
            tel = lab.tel,
            youtube = lab.youtube,
            websiteURL = lab.websiteURL,
            pdf = pdf,
            ko = lab.translationOf(LanguageType.KO)?.let { LabTranslationDto.of(it) },
            en = lab.translationOf(LanguageType.EN)?.let { LabTranslationDto.of(it) }
        )
    }
}

data class LabTranslationDto(
    val name: String,
    val description: String?,
    val location: String?,
    val groupName: String?,
    val professors: List<LabProfessorResponse>
) {
    companion object {
        fun of(translation: LabTranslationEntity) = LabTranslationDto(
            name = translation.name,
            description = translation.description,
            location = translation.location,
            groupName = translation.lab.research?.translationOf(translation.language)?.name,
            // 교수 이름도 같은 언어판으로.
            professors = translation.lab.professors.mapNotNull { professor ->
                professor.translationOf(translation.language)
                    ?.let { LabProfessorResponse(id = professor.id, name = it.name) }
            }
        )
    }
}
