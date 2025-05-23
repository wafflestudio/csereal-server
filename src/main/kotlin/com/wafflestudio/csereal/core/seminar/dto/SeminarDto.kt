package com.wafflestudio.csereal.core.seminar.dto

import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import com.wafflestudio.csereal.core.seminar.database.SeminarEntity
import java.time.LocalDateTime
import java.time.LocalDate

data class SeminarDto(
    val id: Long,
    val title: String,
    val titleForMain: String?,
    val description: String,
    val introduction: String,
    val name: String,
    val speakerURL: String?,
    val speakerTitle: String?,
    val affiliation: String,
    val affiliationURL: String?,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?,
    val location: String,
    val host: String?,
    val additionalNote: String?,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val isPrivate: Boolean,
    val isImportant: Boolean,
    val importantUntil: LocalDate?,
    val prevId: Long?,
    val prevTitle: String?,
    val nextId: Long?,
    val nextTitle: String?,
    val imageURL: String?,
    val attachments: List<AttachmentResponse>?,
    val deleteIds: List<Long>? = null
) {

    companion object {
        fun of(
            entity: SeminarEntity,
            imageURL: String?,
            attachmentResponses: List<AttachmentResponse>,
            prevSeminar: SeminarEntity? = null,
            nextSeminar: SeminarEntity? = null
        ): SeminarDto = entity.run {
            SeminarDto(
                id = this.id,
                title = this.title,
                titleForMain = this.titleForMain,
                description = this.description,
                introduction = this.introduction,
                name = this.name,
                speakerURL = this.speakerURL,
                speakerTitle = this.speakerTitle,
                affiliation = this.affiliation,
                affiliationURL = this.affiliationURL,
                startDate = this.startDate,
                endDate = this.endDate,
                location = this.location,
                host = this.host,
                additionalNote = this.additionalNote,
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                isPrivate = this.isPrivate,
                isImportant = this.isImportant,
                importantUntil = this.importantUntil,
                prevId = prevSeminar?.id,
                prevTitle = prevSeminar?.title,
                nextId = nextSeminar?.id,
                nextTitle = nextSeminar?.title,
                imageURL = imageURL,
                attachments = attachmentResponses
            )
        }
    }
}
