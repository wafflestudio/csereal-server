package com.wafflestudio.csereal.core.undergraduate.dto

import com.wafflestudio.csereal.core.undergraduate.database.UndergraduateEntity
import java.time.LocalDateTime

data class UndergraduateDto(
    val id: Long,
    val postType: String,
    val title: String,
    val description: String,
    val year: Int?,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val isPublic: Boolean,
) {
    companion object {
        fun of(entity: UndergraduateEntity) : UndergraduateDto = entity.run {
            UndergraduateDto(
                id = this.id,
                postType = this.postType,
                title = this.title,
                description = this.description,
                year = this.year,
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                isPublic = this.isPublic,
            )
        }
    }
}