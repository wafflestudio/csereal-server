package com.wafflestudio.csereal.core.introduction.dto


import com.wafflestudio.csereal.core.introduction.database.IntroductionEntity
import java.time.LocalDateTime

data class IntroductionDto(
    val id: Long,
    val postType: String,
    val title: String,
    val description: String,
    val year: Int?,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val isPublic: Boolean,
    val locations: List<String>?
) {
    companion object {
        fun of(entity: IntroductionEntity) : IntroductionDto = entity.run {
            IntroductionDto(
                id = this.id,
                postType = this.postType,
                title = this.title,
                description = this.description,
                year = this.year,
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                isPublic = this.isPublic,
                locations = this.locations.map { it.name }
            )
        }
    }
}