package com.wafflestudio.csereal.core.seminar.database

import com.wafflestudio.csereal.common.cleanTextFromHtml
import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.common.controller.MainImageContentEntityType
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import com.wafflestudio.csereal.core.seminar.dto.SeminarDto
import jakarta.persistence.*

@Entity(name = "seminar")
class SeminarEntity(

    var isDeleted: Boolean = false,

    var title: String,

    @Column(columnDefinition = "mediumtext")
    var description: String,

    @Column(columnDefinition = "mediumtext")
    var plainTextDescription: String,

    @Column(columnDefinition = "mediumtext")
    var introduction: String,

    @Column(columnDefinition = "mediumtext")
    var plainTextIntroduction: String,

    // 연사 정보
    var name: String,
    var speakerURL: String?,
    var speakerTitle: String?,
    var affiliation: String,
    var affiliationURL: String?,

    var startDate: String?,
    var startTime: String?,
    var endDate: String?,
    var endTime: String?,

    var location: String,

    var host: String?,

    var isPublic: Boolean,
    var isImportant: Boolean,

    @Column(columnDefinition = "text")
    var additionalNote: String?,

    @Column(columnDefinition = "text")
    var plainTextAdditionalNote: String?,

    @OneToOne
    var mainImage: MainImageEntity? = null,

    @OneToMany(mappedBy = "seminar", cascade = [CascadeType.ALL], orphanRemoval = true)
    var attachments: MutableList<AttachmentEntity> = mutableListOf(),

    ): BaseTimeEntity(), MainImageContentEntityType, AttachmentContentEntityType {
    override fun bringMainImage(): MainImageEntity? = mainImage
    override fun bringAttachments() = attachments

    companion object {
        fun of(seminarDto: SeminarDto): SeminarEntity {
            return SeminarEntity(
                title = seminarDto.title,
                description = seminarDto.description,
                introduction = seminarDto.introduction,
                name = seminarDto.name,
                speakerURL = seminarDto.speakerURL,
                speakerTitle = seminarDto.speakerTitle,
                affiliation = seminarDto.affiliation,
                affiliationURL = seminarDto.affiliationURL,
                startDate = seminarDto.startDate,
                startTime = seminarDto.startTime,
                endDate = seminarDto.endDate,
                endTime = seminarDto.endTime,
                location = seminarDto.location,
                host = seminarDto.host,
                isPublic = seminarDto.isPublic,
                isImportant = seminarDto.isImportant,
                additionalNote = seminarDto.additionalNote,
            )
        }
    }

    fun update(updateSeminarRequest: SeminarDto) {
        title = updateSeminarRequest.title
        description = updateSeminarRequest.description
        introduction = updateSeminarRequest.introduction
        name = updateSeminarRequest.name
        speakerURL = updateSeminarRequest.speakerURL
        speakerTitle = updateSeminarRequest.speakerTitle
        affiliation = updateSeminarRequest.affiliation
        affiliationURL = updateSeminarRequest.affiliationURL
        startDate = updateSeminarRequest.startDate
        startTime = updateSeminarRequest.startTime
        endDate = updateSeminarRequest.endDate
        endTime = updateSeminarRequest.endTime
        location = updateSeminarRequest.location
        host = updateSeminarRequest.host
        isPublic = updateSeminarRequest.isPublic
        isImportant = updateSeminarRequest.isImportant
        additionalNote = updateSeminarRequest.additionalNote
    }
}