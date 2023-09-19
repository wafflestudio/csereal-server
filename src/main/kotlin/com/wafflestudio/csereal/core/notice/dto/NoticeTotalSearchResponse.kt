package com.wafflestudio.csereal.core.notice.dto

data class NoticeTotalSearchResponse(
    val total: Int,
    val results: List<NoticeTotalSearchElement>
)
