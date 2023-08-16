package com.wafflestudio.csereal.core.seminar.dto

data class SeminarSearchResponse(
    val total: Int,
    val searchList: List<SeminarSearchDto>
) {
}