package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse

class AcademicsYearResponse(
    val year: Int,
    val description: String,
    val attachments: List<AttachmentResponse>
) {
}