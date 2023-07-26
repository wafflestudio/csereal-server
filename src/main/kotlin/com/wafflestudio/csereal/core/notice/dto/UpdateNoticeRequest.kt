package com.wafflestudio.csereal.core.notice.dto

data class UpdateNoticeRequest(
    val title: String?,
    val description: String?,
    val tag: List<Long>?
) {
}