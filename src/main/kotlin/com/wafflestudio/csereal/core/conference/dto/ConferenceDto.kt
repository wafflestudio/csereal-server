package com.wafflestudio.csereal.core.conference.dto

import com.wafflestudio.csereal.core.conference.database.ConferenceEntity

data class ConferenceDto(
    val code: String,
    val abbreviation: String,
    val name: String
) {
    companion object {
        fun of(conferenceEntity: ConferenceEntity): ConferenceDto {
            return ConferenceDto(
                code = conferenceEntity.code,
                abbreviation = conferenceEntity.abbreviation,
                name = conferenceEntity.name
            )
        }
    }
}
