package com.wafflestudio.csereal.core.resource.attachment.dto

data class AttachmentResponse(
    val name: String,
    val url: String,
    val bytes: Long,
) {
}