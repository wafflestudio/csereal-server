package com.wafflestudio.csereal.core.research.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.research.database.ResearchTranslationEntity
import com.wafflestudio.csereal.core.research.type.ResearchType
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import java.time.LocalDateTime

data class ResearchDto(
    val id: Long,
    val postType: ResearchType,
    val language: String,
    val name: String,
    val description: String?,
    val websiteURL: String?,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val labs: List<ResearchLabResponse>?,
    val imageURL: String?,
    val attachments: List<AttachmentResponse>?
) {
    companion object {
        fun of(
            translation: ResearchTranslationEntity,
            imageURL: String?,
            attachmentResponse: List<AttachmentResponse>
        ): ResearchDto {
            val research = translation.research
            return ResearchDto(
                id = research.id,
                postType = research.postType,
                language = LanguageType.makeLowercase(translation.language),
                name = translation.name,
                description = translation.description,
                websiteURL = research.websiteURL,
                createdAt = research.createdAt,
                modifiedAt = research.modifiedAt,
                labs = research.labs.mapNotNull { lab ->
                    lab.translationOf(translation.language)?.let { ResearchLabResponse(id = lab.id, name = it.name) }
                },
                imageURL = imageURL,
                attachments = attachmentResponse
            )
        }
    }
}
