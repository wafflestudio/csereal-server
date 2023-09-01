package com.wafflestudio.csereal.core.seminar.dto

import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import com.wafflestudio.csereal.core.seminar.database.SeminarEntity
import java.time.LocalDate
import java.time.LocalDateTime

data class SeminarDto(
    val id: Long,
    val title: String,
    val description: String,
    val introduction: String,
    val name: String,
    val speakerURL: String?,
    val speakerTitle: String?,
    val affiliation: String,
    val affiliationURL: String?,
    val startDate: String?,
    val startTime: String?,
    val endDate: String?,
    val endTime: String?,
    val location: String,
    val host: String?,
    val additionalNote: String?,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val isPublic: Boolean,
    val isImportant: Boolean,
    val prevId: Long?,
    val prevTitle: String?,
    val nextId: Long?,
    val nextTitle: String?,
    val imageURL: String?,
    val attachments: List<AttachmentResponse>?,
) {

    companion object {
        fun of(entity: SeminarEntity, imageURL: String?, attachmentResponses: List<AttachmentResponse>, prevNext: Array<SeminarEntity?>?): SeminarDto = entity.run {
            SeminarDto(
                id = this.id,
                title = this.title,
                description = this.description,
                introduction = this.introduction,
                name = this.name,
                speakerURL = this.speakerURL,
                speakerTitle = this.speakerTitle,
                affiliation = this.affiliation,
                affiliationURL = this.affiliationURL,
                startDate = this.startDate,
                startTime = this.startTime,
                endDate = this.endDate,
                endTime = this.endTime,
                location = this.location,
                host = this.host,
                additionalNote = this.additionalNote,
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                isPublic = this.isPublic,
                isImportant = this.isImportant,
                prevId = prevNext?.get(0)?.id,
                prevTitle = prevNext?.get(0)?.title,
                nextId = prevNext?.get(1)?.id,
                nextTitle = prevNext?.get(1)?.title,
                imageURL = imageURL,
                attachments = attachmentResponses,
            )
        }

    }

}