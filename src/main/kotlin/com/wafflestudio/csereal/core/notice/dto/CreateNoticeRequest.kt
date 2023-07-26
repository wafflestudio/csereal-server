package com.wafflestudio.csereal.core.notice.dto

import jakarta.validation.constraints.NotBlank

data class CreateNoticeRequest(
    @field:NotBlank(message = "제목은 비어있을 수 없습니다")
    val title: String,

    @field:NotBlank(message = "내용은 비어있을 수 없습니다")
    val description: String,

    val tag: List<Long> = emptyList()
) {
}