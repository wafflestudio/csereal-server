package com.wafflestudio.csereal.core.about.dto


import com.wafflestudio.csereal.core.about.database.AboutEntity
import java.time.LocalDateTime

data class AboutDto(
    val id: Long,
    val name: String,
    val engName: String?,
    val description: String,
    val year: Int?,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val locations: List<String>?
) {
    companion object {
        fun of(entity: AboutEntity) : AboutDto = entity.run {
            AboutDto(
                id = this.id,
                name = this.name,
                engName = this.engName,
                description = this.description,
                year = this.year,
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                locations = this.locations.map { it.name }
            )
        }
    }
}