package com.wafflestudio.csereal.core.notice.dto

import com.querydsl.core.annotations.QueryProjection
import java.time.LocalDateTime

data class NoticeSearchDto @QueryProjection constructor(
    val id: Long,
    val title: String,
    val createdAt: LocalDateTime?,
    val isPinned: Boolean,
    val hasAttachment: Boolean,
) {

}