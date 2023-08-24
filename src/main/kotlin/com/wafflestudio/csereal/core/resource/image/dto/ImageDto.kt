package com.wafflestudio.csereal.core.resource.image.dto

data class ImageDto(
    val filename: String,
    val extension: String,
    val imagesOrder: Int,
    val size: Long,
) {
}