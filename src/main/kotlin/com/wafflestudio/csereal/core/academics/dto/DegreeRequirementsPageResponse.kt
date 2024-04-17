package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.AcademicsEntity
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse

class DegreeRequirementsPageResponse(
    val description: String,
    val attachments: List<AttachmentResponse>
) {
    companion object {
        fun of(entity: AcademicsEntity, attachments: List<AttachmentResponse>) = entity.run {
            DegreeRequirementsPageResponse(
                description = this.description,
                attachments = attachments
            )
        }
    }
}
