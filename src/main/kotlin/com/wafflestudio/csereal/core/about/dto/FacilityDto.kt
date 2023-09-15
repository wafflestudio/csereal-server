package com.wafflestudio.csereal.core.about.dto

import com.wafflestudio.csereal.core.about.database.AboutEntity

data class FacilityDto(
    val id: Long,
    val name: String,
    val description: String,
    val locations: List<String>,
) {
    companion object {
        fun of(entity: AboutEntity): FacilityDto = entity.run {
            FacilityDto(
                id = this.id,
                name = this.name!!,
                description = this.description,
                locations = this.locations.map { it.name }
            )
        }
    }
}