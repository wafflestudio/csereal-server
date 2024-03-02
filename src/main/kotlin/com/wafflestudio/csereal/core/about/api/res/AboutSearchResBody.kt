package com.wafflestudio.csereal.core.about.api.res

data class AboutSearchResBody(
    val total: Long,
    val results: List<AboutSearchElementDto>
)
