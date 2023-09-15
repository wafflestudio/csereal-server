package com.wafflestudio.csereal.core.about.dto.request

import java.time.LocalDateTime

data class AboutRequest(
    val postType: String,
    val id: Long,
    val name: String?,
    val engName: String?,
    val description: String,
    val year: Int?,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val locations: List<String>?,
) {
}