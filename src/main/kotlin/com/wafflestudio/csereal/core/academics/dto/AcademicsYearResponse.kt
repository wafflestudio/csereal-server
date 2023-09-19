package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.AcademicsEntity
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse

class AcademicsYearResponse(
    val year: Int,
    val description: String,
    val attachments: List<AttachmentResponse>
) {
    companion object {
        fun of(entity: AcademicsEntity, attachmentResponses: List<AttachmentResponse>) = entity.run {
            AcademicsYearResponse(
                year = entity.year!!,
                description = entity.description,
                attachments = attachmentResponses
            )
        }
    }
}
