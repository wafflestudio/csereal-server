package com.wafflestudio.csereal.core.main.dto

import java.time.LocalDateTime

data class MainImportantResponse(
    val id: Long,
    val title: String,
    val description: String,
    val createdAt: LocalDateTime?,
    val category: String
) {
    constructor(
        id: Long,
        titleForMain: String?,
        title: String,
        description: String,
        createdAt: LocalDateTime?,
        category: String
    ) : this(
        id = id,
        title = titleForMain ?: title,
        description = description,
        createdAt = createdAt,
        category = category
    )
}
