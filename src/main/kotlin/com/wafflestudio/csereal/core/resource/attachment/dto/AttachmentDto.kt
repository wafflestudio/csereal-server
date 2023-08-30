package com.wafflestudio.csereal.core.resource.attachment.dto

data class AttachmentDto(
    val filename: String,
    val attachmentsOrder: Int,
    val size: Long,
) {
}