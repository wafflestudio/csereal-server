package com.wafflestudio.csereal.core.conference.dto

import com.wafflestudio.csereal.core.conference.database.ConferencePageEntity
import java.time.LocalDateTime

data class ConferencePage(
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val author: String,
    val conferenceList: List<ConferenceDto>
) {
    companion object {
        fun of(conferencePageEntity: ConferencePageEntity): ConferencePage {
            return ConferencePage(
                createdAt = conferencePageEntity.createdAt!!,
                modifiedAt = conferencePageEntity.modifiedAt!!,
                author = conferencePageEntity.author.name,
                conferenceList = conferencePageEntity.conferences.map { ConferenceDto.of(it) }.sortedBy { it.code }
            )
        }
    }
}
