package com.wafflestudio.csereal.core.seminar.dto

data class SeminarSearchResponse(
    val total: Long,
    val results: List<SeminarSearchDto>
)
