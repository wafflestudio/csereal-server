package com.wafflestudio.csereal.core.notice.dto

import com.wafflestudio.csereal.core.notice.database.NoticeEntity
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import java.time.LocalDateTime
import java.time.LocalDate

// TODO: split between dto, response, request data classes
data class NoticeDto(
    val id: Long,
    val title: String,
    val titleForMain: String?,
    val description: String,
    val author: String?,
    val tags: List<String>, // TODO: make nullable
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val isPrivate: Boolean,
    val isPinned: Boolean,
    val pinnedUntil: LocalDate?,
    val isImportant: Boolean,
    val importantUntil: LocalDate?,
    val prevId: Long?,
    val prevTitle: String?,
    val nextId: Long?,
    val nextTitle: String?,
    val attachments: List<AttachmentResponse>?,
    val deleteIds: List<Long>? = null
) {

    companion object {
        fun of(
            entity: NoticeEntity,
            attachmentResponses: List<AttachmentResponse>,
            prevNotice: NoticeEntity? = null,
            nextNotice: NoticeEntity? = null
        ): NoticeDto = entity.run {
            NoticeDto(
                id = this.id,
                title = this.title,
                titleForMain = this.titleForMain,
                description = this.description,
                author = this.author.name,
                tags = this.noticeTags.map { it.tag.name.krName },
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                isPrivate = this.isPrivate,
                isPinned = this.isPinned,
                pinnedUntil = this.pinnedUntil,
                isImportant = this.isImportant,
                importantUntil = this.importantUntil,
                prevId = prevNotice?.id,
                prevTitle = prevNotice?.title,
                nextId = nextNotice?.id,
                nextTitle = nextNotice?.title,
                attachments = attachmentResponses
            )
        }
    }
}
