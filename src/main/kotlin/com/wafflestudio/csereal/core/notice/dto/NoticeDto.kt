package com.wafflestudio.csereal.core.notice.dto

import com.wafflestudio.csereal.core.notice.database.NoticeEntity
import java.time.LocalDateTime

data class NoticeDto(
    val id: Long,
    val isDeleted: Boolean,
    val title: String,
    val description: String,
    // val postType: String,
    // val authorId: Int,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    // val isPublic: Boolean,
    // val isSlide: Boolean,
    // val isPinned: Boolean,
) {

    companion object {
        fun of(entity: NoticeEntity): NoticeDto = entity.run {
            NoticeDto(
                id = this.id,
                isDeleted = false,
                title = this.title,
                description = this.description,
                // postType = this.postType,
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
//                isPublic = this.isPublic,
//                isSlide = this.isSlide,
//                isPinned = this.isPinned,
            )
        }

    }

}