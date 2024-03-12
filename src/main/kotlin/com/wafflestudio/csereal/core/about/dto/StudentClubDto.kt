package com.wafflestudio.csereal.core.about.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.about.database.AboutEntity
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import java.time.LocalDateTime

data class StudentClubDto(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val id: Long? = null,
    val language: String,
    val name: String,
    val engName: String,
    val description: String,
    val year: Int?,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val locations: List<String>?,
    val imageURL: String?,
    val attachments: List<AttachmentResponse>?
) {
    companion object {
        fun of(
            entity: AboutEntity,
            name: String,
            engName: String,
            imageURL: String?,
            attachmentResponses: List<AttachmentResponse>
        ): StudentClubDto = entity.run {
            StudentClubDto(
                id = this.id,
                language = LanguageType.makeLowercase(this.language),
                name = name,
                engName = engName,
                description = this.description,
                year = this.year,
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                locations = this.locations,
                imageURL = imageURL,
                attachments = attachmentResponses
            )
        }
    }
}
