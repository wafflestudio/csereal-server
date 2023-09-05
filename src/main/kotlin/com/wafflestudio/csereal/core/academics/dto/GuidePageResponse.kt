package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.AcademicsEntity
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse

class GuidePageResponse(
    val description: String,
    val attachments: List<AttachmentResponse>
) {
    companion object {
        fun of(entity: AcademicsEntity, attachmentResponses: List<AttachmentResponse>): GuidePageResponse = entity.run {
            GuidePageResponse(
                description = this.description,
                attachments = attachmentResponses
            )
        }
    }
}