package com.wafflestudio.csereal.core.seminar.dto

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
    val isYearLast: Boolean,
    val isPrivate: Boolean
) {
}
