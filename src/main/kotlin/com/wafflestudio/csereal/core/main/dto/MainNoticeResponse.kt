package com.wafflestudio.csereal.core.main.dto

import com.querydsl.core.annotations.QueryProjection
import java.time.LocalDateTime

data class MainNoticeResponse(
    val id: Long,
    val title: String,
    val createdAt: LocalDateTime?
) {

    @QueryProjection constructor (
        id: Long,
        title: String,
        createdAt: LocalDateTime?,
        isPinned: Boolean
    ) : this(id, title, createdAt) {
    }
}
