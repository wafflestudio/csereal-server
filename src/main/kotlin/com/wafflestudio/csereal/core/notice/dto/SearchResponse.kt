package com.wafflestudio.csereal.core.notice.dto

data class SearchResponse(
    val total: Int,
    val searchList: List<SearchDto>
) {

}