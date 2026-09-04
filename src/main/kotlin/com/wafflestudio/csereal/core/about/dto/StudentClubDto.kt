package com.wafflestudio.csereal.core.about.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.about.database.AboutTranslationEntity
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import java.time.LocalDateTime

data class StudentClubDto(
    val id: Long,
    val language: String,
    val name: String,
    val engName: String,
    val description: String,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val locations: List<String>?,
    val imageURL: String?,
    val attachments: List<AttachmentResponse>?
) {
    companion object {
        fun of(
            translation: AboutTranslationEntity,
            name: String,
            engName: String,
            imageURL: String?,
            attachmentResponses: List<AttachmentResponse>
        ): StudentClubDto = StudentClubDto(
            id = translation.about.id,
            language = LanguageType.makeLowercase(translation.language),
            name = name,
            engName = engName,
            description = translation.description,
            createdAt = translation.about.createdAt,
            modifiedAt = translation.about.modifiedAt,
            locations = translation.locations,
            imageURL = imageURL,
            attachments = attachmentResponses
        )
    }
}
