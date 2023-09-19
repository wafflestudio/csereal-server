package com.wafflestudio.csereal.core.seminar.dto

data class SeminarSearchResponse(
    val total: Long,
    val searchList: List<SeminarSearchDto>
)
