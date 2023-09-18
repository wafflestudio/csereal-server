package com.wafflestudio.csereal.core.about.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.wafflestudio.csereal.core.about.database.AboutEntity

data class StudentClubDto(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val id: Long? = null,
    val name: String,
    val engName: String,
    val description: String,
) {
    companion object {
        fun of(entity: AboutEntity): StudentClubDto = entity.run {
            StudentClubDto(
                id = this.id,
                name = this.name!!,
                engName = this.engName!!,
                description = this.description
            )
        }
    }
}