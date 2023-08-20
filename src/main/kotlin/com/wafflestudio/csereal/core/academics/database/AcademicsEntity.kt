package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.academics.dto.AcademicsDto
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity(name = "academics")
class AcademicsEntity(
    @Enumerated(EnumType.STRING)
    var studentType: AcademicsStudentType,

    @Enumerated(EnumType.STRING)
    var postType: AcademicsPostType,

    var name: String,
    var description: String,
    var year: Int?,
    var isPublic: Boolean,

): BaseTimeEntity() {
    companion object {
        fun of(studentType: AcademicsStudentType, postType: AcademicsPostType, academicsDto: AcademicsDto): AcademicsEntity {
            return AcademicsEntity(
                studentType = studentType,
                postType = postType,
                name = academicsDto.name,
                description = academicsDto.description,
                year = academicsDto.year,
                isPublic = academicsDto.isPublic,
            )
        }
    }
}