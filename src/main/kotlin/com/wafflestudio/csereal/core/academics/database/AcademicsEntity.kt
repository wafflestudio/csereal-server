package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.core.academics.dto.AcademicsDto
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import jakarta.persistence.*

@Entity(name = "academics")
class AcademicsEntity(
    @Enumerated(EnumType.STRING)
    var studentType: AcademicsStudentType,

    @Enumerated(EnumType.STRING)
    var postType: AcademicsPostType,

    var name: String?,
    var description: String,
    var year: Int?,
    var time: String?,

    @OneToMany(mappedBy = "academics", cascade = [CascadeType.ALL], orphanRemoval = true)
    var attachments: MutableList<AttachmentEntity> = mutableListOf()

) : BaseTimeEntity(), AttachmentContentEntityType {
    override fun bringAttachments() = attachments

    companion object {
        fun of(studentType: AcademicsStudentType, postType: AcademicsPostType, academicsDto: AcademicsDto): AcademicsEntity {
            return AcademicsEntity(
                studentType = studentType,
                postType = postType,
                name = academicsDto.name,
                description = academicsDto.description,
                year = academicsDto.year,
                time = academicsDto.time
            )
        }
    }
}
