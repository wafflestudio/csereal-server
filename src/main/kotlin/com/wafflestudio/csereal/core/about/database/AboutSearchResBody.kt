package com.wafflestudio.csereal.core.about.database

data class AboutSearchResBody(
    val total: Long,
    val results: List<AboutSearchElementDto>
)
