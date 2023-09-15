package com.wafflestudio.csereal.core.about.dto.request

import java.time.LocalDateTime

data class AboutRequest(
    val postType: String,
    val description: String,
) {
}