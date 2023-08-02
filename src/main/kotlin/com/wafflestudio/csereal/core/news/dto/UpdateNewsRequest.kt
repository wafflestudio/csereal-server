package com.wafflestudio.csereal.core.news.dto

data class UpdateNewsRequest(
    val title: String?,
    val description: String?,
    val tags: List<Long>?,
    val isPublic: Boolean?,
    val isSlide: Boolean?,
    val isPinned: Boolean?,
) {
}