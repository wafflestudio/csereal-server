package com.wafflestudio.csereal.common.enums

enum class ContentSearchSortType {
    DATE,
    RELEVANCE;

    @com.fasterxml.jackson.annotation.JsonValue
    fun toValue() = name.lowercase().replace('_', '-')
}
