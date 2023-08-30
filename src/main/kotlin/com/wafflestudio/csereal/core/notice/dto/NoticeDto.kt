package com.wafflestudio.csereal.core.notice.dto

import com.wafflestudio.csereal.core.notice.database.NoticeEntity
import java.time.LocalDateTime

data class NoticeDto(
    val id: Long,
    val title: String,
    val description: String,
    // val authorId: Int,
    val tags: List<String>,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val isPublic: Boolean,
    val isPinned: Boolean,
    val prevId: Long?,
    val prevTitle: String?,
    val nextId: Long?,
    val nextTitle: String?
) {

    companion object {
        fun of(entity: NoticeEntity, prevNext: Array<NoticeEntity?>?): NoticeDto = entity.run {
            NoticeDto(
                id = this.id,
                title = this.title,
                description = this.description,
                tags = this.noticeTags.map { it.tag.name },
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                isPublic = this.isPublic,
                isPinned = this.isPinned,
                prevId = prevNext?.get(0)?.id,
                prevTitle = prevNext?.get(0)?.title,
                nextId = prevNext?.get(1)?.id,
                nextTitle = prevNext?.get(1)?.title
            )
        }

    }

}