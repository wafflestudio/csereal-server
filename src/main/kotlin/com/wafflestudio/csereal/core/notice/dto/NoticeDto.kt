package com.wafflestudio.csereal.core.notice.dto

import com.wafflestudio.csereal.core.notice.database.NoticeEntity
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import java.time.LocalDateTime

data class NoticeDto(
    val id: Long,
    val title: String,
    val description: String,
    val author: String?,
    val tags: List<String>,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val isPublic: Boolean,
    val isPinned: Boolean,
    val isImportant: Boolean,
    val prevId: Long?,
    val prevTitle: String?,
    val nextId: Long?,
    val nextTitle: String?,
    val attachments: List<AttachmentResponse>?,
) {

    companion object {
        fun of(
            entity: NoticeEntity,
            attachmentResponses: List<AttachmentResponse>,
            prevNext: Array<NoticeEntity?>?
        ): NoticeDto = entity.run {
            NoticeDto(
                id = this.id,
                title = this.title,
                description = this.description,
                author = this.author.name,
                tags = this.noticeTags.map { it.tag.name },
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                isPublic = this.isPublic,
                isPinned = this.isPinned,
                isImportant = this.isImportant,
                prevId = prevNext?.get(0)?.id,
                prevTitle = prevNext?.get(0)?.title,
                nextId = prevNext?.get(1)?.id,
                nextTitle = prevNext?.get(1)?.title,
                attachments = attachmentResponses,
            )
        }

    }

}
