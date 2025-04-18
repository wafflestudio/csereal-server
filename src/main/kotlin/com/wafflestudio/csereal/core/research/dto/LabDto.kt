package com.wafflestudio.csereal.core.research.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.research.database.LabEntity
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
        fun of(entity: LabEntity, pdf: AttachmentResponse?): LabDto = entity.run {
            LabDto(
                id = this.id,
                language = LanguageType.makeLowercase(entity.language),
                name = this.name,
                professors = this.professors.map { LabProfessorResponse(id = it.id, name = it.name) },
                location = this.location,
                tel = this.tel,
                acronym = this.acronym,
                pdf = pdf,
                youtube = this.youtube,
                group = this.research?.let { LabGroupDto.of(it) },
                description = this.description,
                websiteURL = this.websiteURL
            )
        }
    }
}

data class LabGroupDto(
    val id: Long,
    val name: String
) {
    companion object {
        fun of(entity: ResearchEntity): LabGroupDto {
            if (entity.postType != ResearchType.GROUPS) {
                throw IllegalArgumentException("ResearchEntity is not a group")
            }

            return LabGroupDto(entity.id, entity.name)
        }
    }
}
