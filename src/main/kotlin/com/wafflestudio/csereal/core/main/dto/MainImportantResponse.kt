package com.wafflestudio.csereal.core.main.dto

import java.time.LocalDateTime

data class MainImportantResponse(
    val id: Long,
    val title: String,
    val description: String,
    val createdAt: LocalDateTime?,
    val category: String
)
