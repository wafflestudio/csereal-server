package com.wafflestudio.csereal.core.conference.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.conference.database.ConferenceEntity

data class ConferenceDto(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val id: Long? = null,
    val language: String,
    val code: String,
    val abbreviation: String,
    val name: String
) {
    companion object {
        fun of(conferenceEntity: ConferenceEntity): ConferenceDto {
            return ConferenceDto(
                id = conferenceEntity.id,
                language = LanguageType.makeLowercase(conferenceEntity.language),
                code = conferenceEntity.code,
                abbreviation = conferenceEntity.abbreviation,
                name = conferenceEntity.name
            )
        }
    }
}
