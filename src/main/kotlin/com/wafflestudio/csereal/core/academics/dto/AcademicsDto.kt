package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.AcademicsEntity
import java.time.LocalDateTime

data class AcademicsDto(
    val id: Long,
    val name: String,
    val description: String,
    val year: Int?,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val isPublic: Boolean,
) {
    companion object {
        fun of(entity: AcademicsEntity) : AcademicsDto = entity.run {
            AcademicsDto(
                id = this.id,
                name = this.name,
                description = this.description,
                year = this.year,
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                isPublic = this.isPublic,
            )
        }
    }
}