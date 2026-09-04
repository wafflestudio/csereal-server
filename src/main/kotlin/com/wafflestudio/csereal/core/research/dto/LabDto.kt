package com.wafflestudio.csereal.core.research.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.research.database.LabTranslationEntity
import com.wafflestudio.csereal.core.research.database.ResearchEntity
import com.wafflestudio.csereal.core.research.type.ResearchType
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse

data class LabDto(
    val id: Long,
    val language: String,
    val name: String,
    val professors: List<LabProfessorResponse>,
    val location: String?,
    val tel: String?,
    val acronym: String?,
    val pdf: AttachmentResponse?,
    val youtube: String?,
    val group: LabGroupDto?,
    val description: String?,
    val websiteURL: String?
) {
    companion object {
        fun of(translation: LabTranslationEntity, pdf: AttachmentResponse?): LabDto {
            val lab = translation.lab
            return LabDto(
                id = lab.id,
                language = LanguageType.makeLowercase(translation.language),
                name = translation.name,
                // 교수 이름도 이 번역본과 같은 언어판으로.
                professors = lab.professors.mapNotNull { professor ->
                    professor.translationOf(translation.language)
                        ?.let { LabProfessorResponse(id = professor.id, name = it.name) }
                },
                location = translation.location,
                tel = lab.tel,
                acronym = lab.acronym,
                pdf = pdf,
                youtube = lab.youtube,
                group = lab.research?.let { LabGroupDto.of(it, translation.language) },
                description = translation.description,
                websiteURL = lab.websiteURL
            )
        }
    }
}

data class LabGroupDto(
    val id: Long,
    val name: String
) {
    companion object {
        fun of(entity: ResearchEntity, language: LanguageType): LabGroupDto {
            if (entity.postType != ResearchType.GROUPS) {
                throw IllegalArgumentException("ResearchEntity is not a group")
            }

            return LabGroupDto(entity.id, entity.translationOf(language)?.name ?: "")
        }
    }
}
