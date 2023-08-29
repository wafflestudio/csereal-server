package com.wafflestudio.csereal.core.seminar.dto

import com.wafflestudio.csereal.core.resource.image.database.ImageEntity
import com.wafflestudio.csereal.core.seminar.database.SeminarEntity

data class SeminarDto(
    val id: Long,
    val title: String,
    val description: String,
    val introduction: String,
    val category: String,
    val name: String,
    val speakerUrl: String?,
    val speakerTitle: String?,
    val affiliation: String,
    val affiliationUrl: String?,
    val startDate: String?,
    val startTime: String?,
    val endDate: String?,
    val endTime: String?,
    val location: String,
    val host: String?,
    // val seminarFile: File,
    val additionalNote: String?,
    val isPublic: Boolean,
    val isSlide: Boolean,
    val prevId: Long?,
    val prevTitle: String?,
    val nextId: Long?,
    val nextTitle: String?,
    val imageURL: String?,
) {

    companion object {
        fun of(entity: SeminarEntity, imageURL: String?, prevNext: Array<SeminarEntity?>?): SeminarDto = entity.run {
            SeminarDto(
                id = this.id,
                title = this.title,
                description = this.description,
                introduction = this.introduction,
                category = this.category,
                name = this.name,
                speakerUrl = this.speakerUrl,
                speakerTitle = this.speakerTitle,
                affiliation = this.affiliation,
                affiliationUrl = this.affiliationUrl,
                startDate = this.startDate,
                startTime = this.startTime,
                endDate = this.endDate,
                endTime = this.endTime,
                location = this.location,
                host = this.host,
                additionalNote = this.additionalNote,
                isPublic = this.isPublic,
                isSlide = this.isSlide,
                prevId = prevNext?.get(0)?.id,
                prevTitle = prevNext?.get(0)?.title,
                nextId = prevNext?.get(1)?.id,
                nextTitle = prevNext?.get(1)?.title,
                imageURL = imageURL,
            )
        }

    }

}