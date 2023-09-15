package com.wafflestudio.csereal.core.about.dto

import com.wafflestudio.csereal.core.about.database.AboutEntity

data class DirectionDto(
    val id: Long,
    val name: String,
    val engName: String,
    val description: String,
) {
    companion object {
        fun of(entity: AboutEntity): DirectionDto = entity.run {
            DirectionDto(
                id = this.id,
                name = this.name!!,
                engName = this.engName!!,
                description = this.description
            )
        }
    }
}