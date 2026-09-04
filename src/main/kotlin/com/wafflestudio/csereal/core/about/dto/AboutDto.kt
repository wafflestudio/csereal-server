package com.wafflestudio.csereal.core.about.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.about.database.AboutTranslationEntity
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import java.time.LocalDateTime

data class AboutDto(
    // 콘텐츠 자체(부모)의 id 다 — 편집·삭제가 이 id 를 쓴다.
    val id: Long,
    val language: String,
    val name: String?,
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
            imageURL: String?,
            attachmentResponses: List<AttachmentResponse>
        ): AboutDto = AboutDto(
            id = translation.about.id,
            language = LanguageType.makeLowercase(translation.language),
            name = translation.name,
            description = translation.description,
            createdAt = translation.about.createdAt,
            modifiedAt = translation.about.modifiedAt,
            locations = translation.locations,
            imageURL = imageURL,
            attachments = attachmentResponses
        )
    }
}
