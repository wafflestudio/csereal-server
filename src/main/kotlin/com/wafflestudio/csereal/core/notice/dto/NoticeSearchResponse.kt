package com.wafflestudio.csereal.core.notice.dto

data class NoticeSearchResponse(
    val total: Long,
    val searchList: List<NoticeSearchDto>
)
