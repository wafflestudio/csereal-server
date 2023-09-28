package com.wafflestudio.csereal.core.conference.dto

data class ConferenceModifyRequest(
    val newConferenceList: List<ConferenceDto>,
    val modifiedConferenceList: List<ConferenceDto>,
    val deleteConferenceIdList: List<Long>
)
