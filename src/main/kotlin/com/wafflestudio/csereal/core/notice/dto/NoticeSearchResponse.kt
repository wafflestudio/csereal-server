package com.wafflestudio.csereal.core.notice.dto

data class NoticeSearchResponse(
    val total: Int,
    val searchList: List<NoticeSearchDto>
) {

}
