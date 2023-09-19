package com.wafflestudio.csereal.core.main.dto

import com.querydsl.core.annotations.QueryProjection
import java.time.LocalDateTime

data class MainSlideResponse @QueryProjection constructor(
    val id: Long,
    val title: String,
    val imageURL: String?,
    val createdAt: LocalDateTime?,
    val description: String,
) {

}