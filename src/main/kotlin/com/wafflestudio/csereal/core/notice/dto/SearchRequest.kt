package com.wafflestudio.csereal.core.notice.dto

data class SearchRequest(
    val tags: List<Long>?,
    val keyword: String?
) {
}