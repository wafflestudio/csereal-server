package com.wafflestudio.csereal.core.research.dto

import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.research.database.ResearchEntity
import com.wafflestudio.csereal.core.research.database.ResearchPostType
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import java.time.LocalDateTime

data class ResearchDto(
    val id: Long,
    val postType: ResearchPostType,
    val language: String,
    val name: String,
    val description: String?,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val labs: List<ResearchLabResponse>?,
    val imageURL: String?,
    val attachments: List<AttachmentResponse>?
) {
    companion object {
        fun of(entity: ResearchEntity, imageURL: String?, attachmentResponse: List<AttachmentResponse>) = entity.run {
            ResearchDto(
                id = this.id,
                postType = this.postType,
                language = LanguageType.makeLowercase(entity.language),
                name = this.name,
                description = this.description,
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                labs = this.labs.map { ResearchLabResponse(id = it.id, name = it.name) },
                imageURL = imageURL,
                attachments = attachmentResponse
            )
        }
    }
}
