package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.core.academics.dto.ScholarshipDto
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import jakarta.persistence.*

@Entity(name = "scholarship")
class ScholarshipEntity(
    @Enumerated(EnumType.STRING)
    var studentType: AcademicsStudentType,

    val name: String,

    @Column(columnDefinition = "text")
    val description: String,

    @OneToMany(mappedBy = "scholarship", cascade = [CascadeType.ALL], orphanRemoval = true)
    var attachments: MutableList<AttachmentEntity> = mutableListOf(),

) : BaseTimeEntity(), AttachmentContentEntityType {
    override fun bringAttachments() = attachments

    companion object {
        fun of(studentType: AcademicsStudentType, scholarshipDto: ScholarshipDto): ScholarshipEntity {
            return ScholarshipEntity(
                studentType = studentType,
                name = scholarshipDto.name,
                description = scholarshipDto.description,
            )
        }
    }
}