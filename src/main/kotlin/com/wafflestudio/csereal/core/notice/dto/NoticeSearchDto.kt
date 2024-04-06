package com.wafflestudio.csereal.core.notice.dto

import com.querydsl.core.annotations.QueryProjection
import com.wafflestudio.csereal.core.notice.database.NoticeEntity
import java.time.LocalDateTime

data class NoticeSearchDto(
    val id: Long,
    val title: String,
    val createdAt: LocalDateTime?,
    val isPinned: Boolean,
    val hasAttachment: Boolean,
    val isPrivate: Boolean
) {
    @QueryProjection constructor(
        id: Long,
        title: String,
        createdAt: LocalDateTime?,
        isPinned: Boolean,
        hasAttachment: Boolean,
        isPrivate: Boolean,
        score: Double?
    ) : this(id, title, createdAt, isPinned, hasAttachment, isPrivate)

    constructor(entity: NoticeEntity, hasAttachment: Boolean) : this(
        entity.id,
        entity.title,
        entity.createdAt,
        entity.isPinned,
        hasAttachment,
        entity.isPrivate
    )
}
