package com.wafflestudio.csereal.core.main.dto

data class MainResponse(
    val slides: List<MainSlideResponse>,
    val notices: NoticesResponse,
    val importants: List<MainImportantResponse>
)
