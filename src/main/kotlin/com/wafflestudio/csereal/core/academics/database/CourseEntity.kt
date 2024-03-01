package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.academics.dto.CourseDto
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import jakarta.persistence.*

@Entity(name = "course")
class CourseEntity(
    var isDeleted: Boolean = false,

    var studentType: AcademicsStudentType,

    @Enumerated(EnumType.STRING)
    var language: LanguageType,

    var classification: String,
    var code: String,
    var name: String,
    var credit: Int,
    var grade: String,

    @Column(columnDefinition = "mediumText")
    var description: String?,

    @OneToMany(mappedBy = "course", cascade = [CascadeType.ALL], orphanRemoval = true)
    var attachments: MutableList<AttachmentEntity> = mutableListOf(),

    @OneToOne(mappedBy = "course", cascade = [CascadeType.ALL], orphanRemoval = true)
    var academicsSearch: AcademicsSearchEntity? = null

) : BaseTimeEntity(), AttachmentContentEntityType {
    override fun bringAttachments() = attachments
    companion object {
        fun of(studentType: AcademicsStudentType, languageType: LanguageType, courseDto: CourseDto): CourseEntity {
            return CourseEntity(
                studentType = studentType,
                language = languageType,
                classification = courseDto.classification,
                code = courseDto.code,
                name = courseDto.name.replace(" ", "-"),
                credit = courseDto.credit,
                grade = courseDto.grade,
                description = courseDto.description
            )
        }
    }
}
