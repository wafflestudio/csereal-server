package com.wafflestudio.csereal.core.news.dto

import com.wafflestudio.csereal.core.news.database.NewsEntity
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
    val isPinned: Boolean,
    val prevId: Long?,
    val prevTitle: String?,
    val nextId: Long?,
    val nextTitle: String?,
) {
    companion object {
        fun of(entity: NewsEntity, prevNext: Array<NewsEntity?>) : NewsDto = entity.run {
            NewsDto(
                id = this.id,
                title = this.title,
                description = this.description,
                tags = this.newsTags.map { it.tag.name },
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                isPublic = this.isPublic,
                isSlide = this.isSlide,
                isPinned = this.isPinned,
                prevId = prevNext[0]?.id,
                prevTitle = prevNext[0]?.title,
                nextId = prevNext[1]?.id,
                nextTitle = prevNext[1]?.title,

            )
        }
    }
}