package com.wafflestudio.csereal.core.seminar.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.entity.AttachmentAttachable
import com.wafflestudio.csereal.common.entity.MainImageAttachable
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import com.wafflestudio.csereal.core.seminar.dto.SeminarDto
import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.LocalDate

@Entity(name = "seminar")
class SeminarEntity(
    var title: String,

    @Column(columnDefinition = "text")
    var titleForMain: String?,

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

    @Column(columnDefinition = "varchar(2047)")
    var speakerURL: String?,

    var speakerTitle: String?,
    var affiliation: String,
    var affiliationURL: String?,

    var startDate: LocalDateTime?,
    var endDate: LocalDateTime?,

    var location: String,

    var host: String?,

    var isPrivate: Boolean,
    var isImportant: Boolean,
    var importantUntil: LocalDate? = null,

    @Column(columnDefinition = "text")
    var additionalNote: String?,

    @Column(columnDefinition = "text")
    var plainTextAdditionalNote: String?,

    @OneToOne
    override var mainImage: MainImageEntity? = null,

    @OneToMany(mappedBy = "seminar", cascade = [CascadeType.ALL], orphanRemoval = true)
    override var attachments: MutableList<AttachmentEntity> = mutableListOf()

) : BaseTimeEntity(), MainImageAttachable, AttachmentAttachable {

    companion object {
        fun of(seminarDto: SeminarDto): SeminarEntity {
            val plainTextDescription = cleanTextFromHtml(seminarDto.description)
            val plainTextIntroduction = cleanTextFromHtml(seminarDto.introduction)
            val plainTextAdditionalNote = seminarDto.additionalNote?.let { cleanTextFromHtml(it) }

            return SeminarEntity(
                title = seminarDto.title,
                titleForMain = seminarDto.titleForMain,
                description = seminarDto.description,
                plainTextDescription = plainTextDescription,
                introduction = seminarDto.introduction,
                plainTextIntroduction = plainTextIntroduction,
                name = seminarDto.name,
                speakerURL = seminarDto.speakerURL,
                speakerTitle = seminarDto.speakerTitle,
                affiliation = seminarDto.affiliation,
                affiliationURL = seminarDto.affiliationURL,
                startDate = seminarDto.startDate,
                endDate = seminarDto.endDate,
                location = seminarDto.location,
                host = seminarDto.host,
                isPrivate = seminarDto.isPrivate,
                isImportant = seminarDto.isImportant,
                importantUntil = if (seminarDto.isImportant) seminarDto.importantUntil else null,
                additionalNote = seminarDto.additionalNote,
                plainTextAdditionalNote = plainTextAdditionalNote
            )
        }
    }

    fun update(updateSeminarRequest: SeminarDto) {
        if (updateSeminarRequest.description != description) {
            description = updateSeminarRequest.description
            plainTextDescription = cleanTextFromHtml(updateSeminarRequest.description)
        }

        if (updateSeminarRequest.introduction != introduction) {
            introduction = updateSeminarRequest.introduction
            plainTextIntroduction = cleanTextFromHtml(updateSeminarRequest.introduction)
        }

        if (updateSeminarRequest.additionalNote != additionalNote) {
            additionalNote = updateSeminarRequest.additionalNote
            plainTextAdditionalNote = updateSeminarRequest.additionalNote?.let { cleanTextFromHtml(it) }
        }

        title = updateSeminarRequest.title
        titleForMain = updateSeminarRequest.titleForMain
        introduction = updateSeminarRequest.introduction
        name = updateSeminarRequest.name
        speakerURL = updateSeminarRequest.speakerURL
        speakerTitle = updateSeminarRequest.speakerTitle
        affiliation = updateSeminarRequest.affiliation
        affiliationURL = updateSeminarRequest.affiliationURL
        startDate = updateSeminarRequest.startDate
        endDate = updateSeminarRequest.endDate
        location = updateSeminarRequest.location
        host = updateSeminarRequest.host
        isPrivate = updateSeminarRequest.isPrivate
        isImportant = updateSeminarRequest.isImportant
        importantUntil = if (updateSeminarRequest.isImportant) updateSeminarRequest.importantUntil else null
    }

    override fun getAttachmentFolder() = "attachment/seminar"

    override fun getMainImageFolder() = "mainImage/seminar"
}
