package com.wafflestudio.csereal.core.seminar.dto

import com.querydsl.core.annotations.QueryProjection

data class SeminarSearchDto @QueryProjection constructor(
    val id: Long,
    val title: String,
    val description: String,
    val name: String,
    val affiliation: String?,
    val startDate: String?,
    val location: String,
    val imageURL: String?,
    val isYearLast: Boolean,
) {
}