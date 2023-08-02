package com.wafflestudio.csereal.core.news.dto

import com.querydsl.core.annotations.QueryProjection
import java.time.LocalDateTime

data class NewsSearchDto @QueryProjection constructor(
    val id: Long,
    val title: String,
    var summary: String,
    val createdAt: LocalDateTime?,
    var tags: List<Long>?
)