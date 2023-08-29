package com.wafflestudio.csereal.core.seminar.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.ImageContentEntityType
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import com.wafflestudio.csereal.core.seminar.dto.SeminarDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToOne

@Entity(name = "seminar")
class SeminarEntity(

    var isDeleted: Boolean = false,

    var title: String,

    @Column(columnDefinition = "text")
    var description: String,

    @Column(columnDefinition = "text")
    var introduction: String,

    var category: String,

    // 연사 정보
    var name: String,
    var speakerUrl: String?,
    var speakerTitle: String?,
    var affiliation: String,
    var affiliationUrl: String?,

    var startDate: String?,
    var startTime: String?,
    var endDate: String?,
    var endTime: String?,

    var location: String,

    var host: String?,

    // var seminarFile: File,

    var isPublic: Boolean,

    var isSlide: Boolean,

    var additionalNote: String?,

    @OneToOne
    var mainImage: MainImageEntity? = null,
): BaseTimeEntity(), ImageContentEntityType {
    override fun bringMainImage(): MainImageEntity? = mainImage

    companion object {
        fun of(seminarDto: SeminarDto): SeminarEntity {
            return SeminarEntity(
                title = seminarDto.title,
                description = seminarDto.description,
                introduction = seminarDto.introduction,
                category = seminarDto.category,
                name = seminarDto.name,
                speakerUrl = seminarDto.speakerUrl,
                speakerTitle = seminarDto.speakerTitle,
                affiliation = seminarDto.affiliation,
                affiliationUrl = seminarDto.affiliationUrl,
                startDate = seminarDto.startDate,
                startTime = seminarDto.startTime,
                endDate = seminarDto.endDate,
                endTime = seminarDto.endTime,
                location = seminarDto.location,
                host = seminarDto.host,
                additionalNote = seminarDto.additionalNote,
                isPublic = seminarDto.isPublic,
                isSlide = seminarDto.isSlide,
            )
        }
    }

    fun update(updateSeminarRequest: SeminarDto) {
        title = updateSeminarRequest.title
        description = updateSeminarRequest.description
        introduction = updateSeminarRequest.introduction
        category = updateSeminarRequest.category
        name = updateSeminarRequest.name
        speakerUrl = updateSeminarRequest.speakerUrl
        speakerTitle = updateSeminarRequest.speakerTitle
        affiliation = updateSeminarRequest.affiliation
        affiliationUrl = updateSeminarRequest.affiliationUrl
        startDate = updateSeminarRequest.startDate
        startTime = updateSeminarRequest.startTime
        endDate = updateSeminarRequest.endDate
        endTime = updateSeminarRequest.endTime
        location = updateSeminarRequest.location
        host = updateSeminarRequest.host
        additionalNote = updateSeminarRequest.additionalNote
        isPublic = updateSeminarRequest.isPublic
        isSlide = updateSeminarRequest.isSlide
    }
}