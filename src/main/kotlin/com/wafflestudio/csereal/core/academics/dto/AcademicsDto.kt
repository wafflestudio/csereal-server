package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.academics.database.AcademicsEntity
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import java.time.LocalDateTime

data class AcademicsDto(
    val id: Long = -1, // TODO: Seperate to multiple DTOs or set this as nullable
    val language: String,
    val name: String,
    val description: String,
    val year: Int? = null,
    val createdAt: LocalDateTime? = null,
    val modifiedAt: LocalDateTime? = null,
    val attachments: List<AttachmentResponse>? = null
) {
    companion object {
        fun of(entity: AcademicsEntity, attachmentResponses: List<AttachmentResponse>): AcademicsDto = entity.run {
            AcademicsDto(
                id = this.id,
                language = LanguageType.makeLowercase(this.language),
                name = this.name,
                description = this.description,
                year = this.year,
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                attachments = attachmentResponses
            )
        }
    }
}
