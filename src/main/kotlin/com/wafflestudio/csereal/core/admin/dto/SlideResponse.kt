package com.wafflestudio.csereal.core.admin.dto

import java.time.LocalDateTime

class SlideResponse(
    val id: Long,
    val title: String,
    val createdAt: LocalDateTime?
)
