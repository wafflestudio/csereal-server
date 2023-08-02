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
    val isSlide: Boolean,
    val isPinned: Boolean,
    val prevId: Long?,
    val nextId: Long?,
) {

    companion object {
        fun of(entity: NoticeEntity, prevNext: Array<Long?>?): NoticeDto = entity.run {
            NoticeDto(
                id = this.id,
                title = this.title,
                description = this.description,
                tags = this.noticeTags.map { it.tag.name },
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                isPublic = this.isPublic,
                isSlide = this.isSlide,
                isPinned = this.isPinned,
                prevId = prevNext?.get(0),
                nextId = prevNext?.get(1)
            )
        }

    }

}