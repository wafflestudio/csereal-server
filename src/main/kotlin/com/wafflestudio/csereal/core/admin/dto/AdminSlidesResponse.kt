package com.wafflestudio.csereal.core.admin.dto

data class AdminSlidesResponse(
    val total: Long,
    val slides: List<AdminSlideElement> = listOf()
)
