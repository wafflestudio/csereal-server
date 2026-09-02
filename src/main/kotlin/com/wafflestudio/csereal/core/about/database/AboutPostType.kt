package com.wafflestudio.csereal.core.about.database

import com.fasterxml.jackson.annotation.JsonValue

enum class AboutPostType {
    OVERVIEW, GREETINGS, HISTORY, FUTURE_CAREERS, CONTACT, STUDENT_CLUBS, FACILITIES, DIRECTIONS;

    @JsonValue
    fun toValue() = name.lowercase().replace('_', '-')
}
