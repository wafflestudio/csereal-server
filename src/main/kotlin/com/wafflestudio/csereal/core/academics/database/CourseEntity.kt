package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.common.controller.MainImageContentEntityType
import com.wafflestudio.csereal.core.academics.dto.CourseDto
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne

@Entity(name = "course")
class CourseEntity(
    var isDeleted: Boolean = false,

    var studentType: AcademicsStudentType,

    var classification: String,

    var code: String,

    var name: String,

    var credit: Int,

    var grade: String,

    var description: String?,

    @OneToMany(mappedBy = "course", cascade = [CascadeType.ALL], orphanRemoval = true)
    var attachments: MutableList<AttachmentEntity> = mutableListOf(),

): BaseTimeEntity(), AttachmentContentEntityType {
    override fun bringAttachments() = attachments
    companion object {
        fun of(studentType: AcademicsStudentType, courseDto: CourseDto): CourseEntity {
            return CourseEntity(
                studentType = studentType,
                classification = courseDto.classification,
                code = courseDto.code,
                name = courseDto.name.replace(" ","-"),
                credit = courseDto.credit,
                grade = courseDto.grade,
                description = courseDto.description
            )
        }
    }
}