package com.wafflestudio.csereal.core.notice.dto

data class UpdateNoticeRequest(
    val title: String?,
    val description: String?,
    val tags: List<Long>?,
    val isPublic: Boolean?,
    val isSlide: Boolean?,
    val isPinned: Boolean?,
) {
}