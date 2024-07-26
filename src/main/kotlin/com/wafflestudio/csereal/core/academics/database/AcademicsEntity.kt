package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.academics.api.req.CreateYearReq
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import jakarta.persistence.*

@Entity(name = "academics")
class AcademicsEntity(
    @Enumerated(EnumType.STRING)
    var studentType: AcademicsStudentType,

    @Enumerated(EnumType.STRING)
    var postType: AcademicsPostType,
    @Enumerated(EnumType.STRING)
    var language: LanguageType,

    var name: String,

    @Column(columnDefinition = "mediumText")
    var description: String,
    var year: Int?,

    @OneToMany(mappedBy = "academics", cascade = [CascadeType.ALL], orphanRemoval = true)
    var attachments: MutableList<AttachmentEntity> = mutableListOf(),

    @OneToOne(mappedBy = "academics", cascade = [CascadeType.ALL], orphanRemoval = true)
    var academicsSearch: AcademicsSearchEntity? = null

) : BaseTimeEntity(), AttachmentContentEntityType {
    override fun bringAttachments() = attachments

    companion object {
        fun createYearResponse(
            studentType: AcademicsStudentType,
            postType: AcademicsPostType,
            languageType: LanguageType,
            request: CreateYearReq
        ): AcademicsEntity {
            return AcademicsEntity(
                studentType = studentType,
                postType = postType,
                language = languageType,
                name = request.name,
                description = request.description,
                year = request.year
            )
        }
    }
}
