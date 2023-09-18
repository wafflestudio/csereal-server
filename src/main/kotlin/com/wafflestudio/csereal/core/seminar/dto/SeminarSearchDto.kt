package com.wafflestudio.csereal.core.seminar.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.querydsl.core.annotations.QueryProjection
import java.time.LocalDateTime

data class SeminarSearchDto @QueryProjection constructor(
    val id: Long,
    val title: String,
    val description: String,
    val name: String,
    val affiliation: String,
    val startDate: LocalDateTime?,
    val location: String,
    val imageURL: String?,
    @get:JsonProperty("isYearLast")
    @param:JsonProperty("isYearLast")
    val isYearLast: Boolean,
    @get:JsonProperty("isPrivate")
    @param:JsonProperty("isPrivate")
    val isPrivate: Boolean
) {
}
