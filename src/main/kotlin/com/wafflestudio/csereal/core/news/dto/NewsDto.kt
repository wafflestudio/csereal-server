package com.wafflestudio.csereal.core.news.dto

import com.wafflestudio.csereal.core.news.database.NewsEntity
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import java.time.LocalDateTime

data class NewsDto(
    val id: Long,
    val title: String,
    val description: String,
    val tags: List<String>,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val isPublic: Boolean,
    val isSlide: Boolean,
    val isImportant: Boolean,
    val prevId: Long?,
    val prevTitle: String?,
    val nextId: Long?,
    val nextTitle: String?,
    val imageURL: String?,
    val attachments: List<AttachmentResponse>?,
) {
    companion object {
        fun of(
            entity: NewsEntity,
            imageURL: String?,
            attachmentResponses: List<AttachmentResponse>,
            prevNews: NewsEntity? = null,
            nextNews: NewsEntity? = null
        ): NewsDto = entity.run {
            NewsDto(
                id = this.id,
                title = this.title,
                description = this.description,
                tags = this.newsTags.map { it.tag.name },
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                isPublic = this.isPublic,
                isSlide = this.isSlide,
                isImportant = this.isImportant,
                prevId = prevNews?.id,
                prevTitle = prevNews?.title,
                nextId = nextNews?.id,
                nextTitle = nextNews?.title,
                imageURL = imageURL,
                attachments = attachmentResponses,
            )
        }
    }
}
