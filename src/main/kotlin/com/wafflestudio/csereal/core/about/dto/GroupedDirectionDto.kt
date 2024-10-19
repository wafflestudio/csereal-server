package com.wafflestudio.csereal.core.about.dto

import com.wafflestudio.csereal.core.about.database.AboutEntity

data class GroupedDirectionDto(
    val ko: DirDto,
    val en: DirDto
)

data class DirDto(
    val id: Long,
    val name: String,
    val description: String
) {
    companion object {
        fun of(aboutEntity: AboutEntity): DirDto {
            return DirDto(
                id = aboutEntity.id,
                name = aboutEntity.name!!,
                description = aboutEntity.description
            )
        }
    }
}
