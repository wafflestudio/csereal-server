package com.wafflestudio.csereal.core.conference.dto

data class ConferenceModifyRequest(
    val newConferenceList: List<ConferenceCreateDto>,
    val modifiedConferenceList: List<ConferenceDto>,
    val deleteConfereceIdList: List<Long>
)
