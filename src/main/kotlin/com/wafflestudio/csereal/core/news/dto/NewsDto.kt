package com.wafflestudio.csereal.core.news.dto

import com.wafflestudio.csereal.core.news.database.NewsEntity
import java.time.LocalDateTime

data class NewsDto(
    val id: Long,
    val isDeleted: Boolean,
    val title: String,
    val description: String,
    val tags: List<Long>,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val isPublic: Boolean,
    val isSlide: Boolean,
    val isPinned: Boolean,
) {
    companion object {
        fun of(entity: NewsEntity) : NewsDto = entity.run {
            NewsDto(
                id = this.id,
                isDeleted = false,
                title = this.title,
                description = this.description,
                tags = this.newsTags.map { it.tag.id },
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                isPublic = this.isPublic,
                isSlide = this.isSlide,
                isPinned = this.isPinned,
            )
        }
    }
}