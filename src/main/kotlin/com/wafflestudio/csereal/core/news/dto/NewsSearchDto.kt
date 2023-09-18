package com.wafflestudio.csereal.core.news.dto

import com.querydsl.core.annotations.QueryProjection
import java.time.LocalDateTime

data class NewsSearchDto @QueryProjection constructor(
    val id: Long,
    val title: String,
    var description: String,
    val createdAt: LocalDateTime?,
    val date: LocalDateTime?,
    var tags: List<String>?,
    var imageURL: String?,
    val isPrivate: Boolean,
)
