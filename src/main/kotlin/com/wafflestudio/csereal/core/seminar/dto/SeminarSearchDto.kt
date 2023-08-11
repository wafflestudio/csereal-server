package com.wafflestudio.csereal.core.seminar.dto

import com.querydsl.core.annotations.QueryProjection

data class SeminarSearchDto @QueryProjection constructor(
    val id: Long,
    val title: String,
    val startDate: String?,
    val isYearLast: Boolean,
    val name: String,
    val affiliation: String?,
    val location: String
) {
}