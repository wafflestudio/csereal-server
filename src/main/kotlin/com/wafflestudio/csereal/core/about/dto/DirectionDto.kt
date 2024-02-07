package com.wafflestudio.csereal.core.about.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.wafflestudio.csereal.core.about.database.AboutEntity

data class DirectionDto(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val id: Long? = null,
    val language: String,
    val name: String,
    val engName: String,
    val description: String
) {
    companion object {
        fun of(entity: AboutEntity): DirectionDto = entity.run {
            DirectionDto(
                id = this.id,
                language = this.language.toString().lowercase(),
                name = this.name!!,
                engName = this.engName!!,
                description = this.description
            )
        }
    }
}
