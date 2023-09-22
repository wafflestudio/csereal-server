package com.wafflestudio.csereal.core.admin.dto

import java.time.LocalDateTime

data class AdminImportantElement(
    val id: Long,
    val title: String,
    val createdAt: LocalDateTime?,
    val category: String
)
