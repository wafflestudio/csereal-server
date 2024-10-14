package com.wafflestudio.csereal.core.about.dto

import com.wafflestudio.csereal.core.about.database.AboutEntity

data class FacReq(
    val name: String,
    val description: String,
    val locations: MutableList<String>
)

data class FacDto(
    val id: Long,
    val name: String,
    val description: String,
    val locations: MutableList<String>,
    val imageURL: String?
) {
    companion object {
        fun of(aboutEntity: AboutEntity, imageURL: String?): FacDto {
            return FacDto(
                id = aboutEntity.id,
                name = aboutEntity.name!!,
                description = aboutEntity.description,
                locations = aboutEntity.locations,
                imageURL = imageURL
            )
        }
    }
}

data class GroupedFacDto(
    val ko: FacDto,
    val en: FacDto
)
