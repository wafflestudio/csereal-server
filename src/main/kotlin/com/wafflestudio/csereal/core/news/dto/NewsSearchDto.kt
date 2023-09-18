package com.wafflestudio.csereal.core.news.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.querydsl.core.annotations.QueryProjection
import java.time.LocalDateTime

data class NewsSearchDto(
    val id: Long,
    val title: String,
    var description: String,
    val createdAt: LocalDateTime?,
    val date: LocalDateTime?,
    var tags: List<String>?,
    var imageURL: String?,
    @get:JsonProperty("isPrivate")
    @param:JsonProperty("isPrivate")
    val isPrivate: Boolean,
)
