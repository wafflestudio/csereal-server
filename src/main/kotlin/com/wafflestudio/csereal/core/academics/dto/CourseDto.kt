package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.CourseEntity
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse

data class CourseDto(
    val id: Long,
    val classification: String,
    val number: String,
    val name: String,
    val credit: Int,
    val year: String,
    val courseURL: String?,
    val description: String?,
    val attachments: List<AttachmentResponse>?,
) {
    companion object {
        fun of(entity: CourseEntity, attachments: List<AttachmentResponse>?): CourseDto = entity.run {
            CourseDto(
                id = this.id,
                classification = this.classification,
                number = this.number,
                name = this.name,
                credit = this.credit,
                year = this.year,
                courseURL = this.courseURL,
                description = this.description,
                attachments = attachments,
            )
        }
    }
}