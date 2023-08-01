package com.wafflestudio.csereal.core.notice.dto

import com.querydsl.core.annotations.QueryProjection
import java.time.LocalDateTime

data class SearchDto @QueryProjection constructor(
    val noticeId: Long,
    val title: String,
    val createdDate: LocalDateTime,
    val isPinned: Boolean,
) {

}