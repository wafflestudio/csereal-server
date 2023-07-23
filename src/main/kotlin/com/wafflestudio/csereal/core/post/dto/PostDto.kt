package com.wafflestudio.csereal.core.post.dto

import com.wafflestudio.csereal.core.post.database.PostEntity
import java.time.LocalDateTime

data class PostDto(
    val id: Long,
    val title: String,
    val description: String,
    // val postType: String,
    // val authorId: Int,
    // val createdAt: LocalDateTime?,
    // val modifiedAt: LocalDateTime?,
    // val isPublic: Boolean,
    // val isSlide: Boolean,
    // val isPinned: Boolean,
) {

    companion object {
        fun of(entity: PostEntity): PostDto = entity.run {
            PostDto(
                id = this.id,
                title = this.title,
                description = this.description
            )
        }

    }

}