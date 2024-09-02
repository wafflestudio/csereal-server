package com.wafflestudio.csereal.core.about.dto

import com.wafflestudio.csereal.core.about.database.AboutEntity

data class GroupedClubDto(
    val ko: ClubDto,
    val en: ClubDto
)

data class ClubDto(
    val id: Long,
    val name: String,
    val description: String,
    val imageURL: String?
) {
    companion object {
        fun of(aboutEntity: AboutEntity, imageURL: String?): ClubDto {
            return ClubDto(aboutEntity.id, aboutEntity.name!!, aboutEntity.description, imageURL)
        }
    }
}
