package com.wafflestudio.csereal.core.council.dto

import java.time.LocalDateTime

data class CouncilDto(
    val id: Long,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?
    // ...
)
