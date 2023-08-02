package com.wafflestudio.csereal.core.news.dto

import jakarta.validation.constraints.NotBlank

data class CreateNewsRequest(
    @field:NotBlank(message = "제목은 비어있을 수 없습니다")
    val title: String,

    @field:NotBlank(message = "내용은 비어있을 수 없습니다")
    val description: String,

    val tags: List<Long> = emptyList(),

    val isPublic: Boolean,

    val isSlide: Boolean,
    val isPinned: Boolean
) {
}