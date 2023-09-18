package com.wafflestudio.csereal.core.notice.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.querydsl.core.annotations.QueryProjection
import com.wafflestudio.csereal.core.notice.database.NoticeEntity
import java.time.LocalDateTime

data class NoticeSearchDto @QueryProjection constructor(
    val id: Long,
    val title: String,
    val createdAt: LocalDateTime?,
    @get:JsonProperty("isPinned")
    @param:JsonProperty("isPinned")
    val isPinned: Boolean,
    val hasAttachment: Boolean,
    @get:JsonProperty("isPrivate")
    @param:JsonProperty("isPrivate")
    val isPrivate: Boolean,
) {
    constructor(entity: NoticeEntity, hasAttachment: Boolean) : this(
        entity.id,
        entity.title,
        entity.createdAt,
        entity.isPinned,
        hasAttachment,
        entity.isPrivate
    )
}
