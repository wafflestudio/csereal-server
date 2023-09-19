package com.wafflestudio.csereal.core.news.dto

data class NewsTotalSearchDto(
    val total: Int,
    val results: List<NewsTotalSearchElement>
)
