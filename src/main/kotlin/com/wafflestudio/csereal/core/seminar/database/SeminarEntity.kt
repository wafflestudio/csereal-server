package com.wafflestudio.csereal.core.seminar.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.entity.AttachmentAttachable
import com.wafflestudio.csereal.common.entity.MainImageAttachable
import com.wafflestudio.csereal.common.sanitize.HtmlContentHolder
import com.wafflestudio.csereal.common.sanitize.HtmlField
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import com.wafflestudio.csereal.core.seminar.api.req.SeminarReqBody
import com.wafflestudio.csereal.common.search.SearchIndexed
import com.wafflestudio.csereal.common.search.SearchType
import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.LocalDate

@Entity(name = "seminar")
class SeminarEntity(
    var title: String,

    @Column(columnDefinition = "text")
    var titleForMain: String?,

    description: String,

    introduction: String,

    // 연사 정보
    var name: String,

    @Column(columnDefinition = "varchar(2047)")
    var speakerURL: String?,

    var speakerTitle: String?,
    var affiliation: String,
    var affiliationURL: String?,

    var startDate: LocalDateTime,
    var endDate: LocalDateTime?,

    var location: String,

    var host: String?,

    var isPrivate: Boolean,
    var isImportant: Boolean,
    var importantUntil: LocalDate? = null,

    additionalNote: String?,

    @OneToOne
    override var mainImage: MainImageEntity? = null,

    @OneToMany(mappedBy = "seminar", cascade = [CascadeType.ALL], orphanRemoval = true)
    override var attachments: MutableList<AttachmentEntity> = mutableListOf()

) : BaseTimeEntity(), MainImageAttachable, AttachmentAttachable, SearchIndexed, HtmlContentHolder {

    override val searchType get() = SearchType.SEMINAR
    override val searchSourceId get() = id

    // 본문 셋과 각각의 평문. 대입할 때마다 함께 갱신해 둘이 어긋날 수 없게 한다
    // (Hibernate 는 필드 접근이라 DB 에서 읽을 땐 이 setter 를 타지 않는다).

    @Column(columnDefinition = "mediumtext")
    var description: String = description
        set(value) {
            field = value
            plainTextDescription = cleanTextFromHtml(value)
        }

    @Column(columnDefinition = "mediumtext")
    var plainTextDescription: String = cleanTextFromHtml(description)

    @Column(columnDefinition = "mediumtext")
    var introduction: String = introduction
        set(value) {
            field = value
            plainTextIntroduction = cleanTextFromHtml(value)
        }

    @Column(columnDefinition = "mediumtext")
    var plainTextIntroduction: String = cleanTextFromHtml(introduction)

    @Column(columnDefinition = "text")
    var additionalNote: String? = additionalNote
        set(value) {
            field = value
            plainTextAdditionalNote = value?.let { cleanTextFromHtml(it) }
        }

    @Column(columnDefinition = "text")
    var plainTextAdditionalNote: String? = additionalNote?.let { cleanTextFromHtml(it) }

    override fun htmlFields() = listOf(
        HtmlField({ description }, { description = it }),
        HtmlField({ introduction }, { introduction = it }),
        HtmlField({ additionalNote }, { additionalNote = it })
    )

    companion object {
        fun of(seminarDto: SeminarReqBody): SeminarEntity {
            return SeminarEntity(
                title = seminarDto.title,
                titleForMain = seminarDto.titleForMain,
                description = seminarDto.description,
                introduction = seminarDto.introduction,
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
                additionalNote = seminarDto.additionalNote
            )
        }
    }

    fun update(updateSeminarRequest: SeminarReqBody) {
        description = updateSeminarRequest.description
        additionalNote = updateSeminarRequest.additionalNote

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

    override fun attach(attachment: AttachmentEntity) {
        attachments.add(attachment)
        attachment.seminar = this
    }
}
