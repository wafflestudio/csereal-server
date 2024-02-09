package com.wafflestudio.csereal.core.about.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.about.database.AboutEntity

data class StudentClubDto(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val id: Long? = null,
    val language: String,
    val name: String,
    val description: String
) {
    companion object {
        fun of(entity: AboutEntity): StudentClubDto = entity.run {
            StudentClubDto(
                id = this.id,
                language = LanguageType.makeLowercase(this.language),
                name = this.name!!,
                description = this.description
            )
        }
    }
}
