package com.wafflestudio.csereal.core.main.dto

import java.time.LocalDateTime

data class MainNoticeResponse(
    val id: Long,
    val title: String,
    val createdAt: LocalDateTime?
) {
}