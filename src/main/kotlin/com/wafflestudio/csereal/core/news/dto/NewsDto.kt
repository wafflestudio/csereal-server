package com.wafflestudio.csereal.core.news.dto

import com.wafflestudio.csereal.core.news.database.NewsEntity
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import java.time.LocalDateTime
import java.time.LocalDate

data class NewsDto(
    val id: Long,
    val title: String,
    val titleForMain: String?,
    val description: String,
    val tags: List<String>,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val date: LocalDateTime,
    val isPrivate: Boolean,
    val isSlide: Boolean,
    val isImportant: Boolean,
    val importantUntil: LocalDate?,
    val prevId: Long?,
    val prevTitle: String?,
    val nextId: Long?,
    val nextTitle: String?,
    val imageURL: String?,
    val attachments: List<AttachmentResponse>?,
    val deleteIds: List<Long>? = null
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
                titleForMain = this.titleForMain,
                description = this.description,
                tags = this.newsTags.map { it.tag.name.krName },
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                date = this.date,
                isPrivate = this.isPrivate,
                isSlide = this.isSlide,
                isImportant = this.isImportant,
                importantUntil = this.importantUntil,
                prevId = prevNews?.id,
                prevTitle = prevNews?.title,
                nextId = nextNews?.id,
                nextTitle = nextNews?.title,
                imageURL = imageURL,
                attachments = attachmentResponses
            )
        }
    }
}
