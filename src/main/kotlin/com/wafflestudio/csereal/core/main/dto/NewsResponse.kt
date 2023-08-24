package com.wafflestudio.csereal.core.main.dto

import com.querydsl.core.annotations.QueryProjection
import java.time.LocalDateTime

data class NewsResponse @QueryProjection constructor(
    val id: Long,
    val title: String,
    val createdAt: LocalDateTime?
) {

}