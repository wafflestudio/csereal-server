package com.wafflestudio.csereal.core.about.dto


import com.fasterxml.jackson.annotation.JsonInclude
import com.wafflestudio.csereal.core.about.database.AboutEntity
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import java.time.LocalDateTime

data class AboutDto(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val id: Long? = null,
    val name: String?,
    val engName: String?,
    val description: String,
    val year: Int?,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val locations: List<String>?,
    val imageURL: String?,
    val attachments: List<AttachmentResponse>?,
) {
    companion object {
        fun of(entity: AboutEntity, imageURL: String?, attachmentResponses: List<AttachmentResponse>) : AboutDto = entity.run {
            AboutDto(
                id = this.id,
                name = this.name,
                engName = this.engName,
                description = this.description,
                year = this.year,
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                locations = this.locations.map { it.name },
                imageURL = imageURL,
                attachments = attachmentResponses,
            )
        }
    }
}