package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.academics.dto.AcademicsDto
import jakarta.persistence.Entity

@Entity(name = "academics")
class AcademicsEntity(
    var to: String,

    var postType: String,

    var name: String,

    var description: String,

    var year: Int?,

    var isPublic: Boolean,

): BaseTimeEntity() {
    companion object {
        fun of(to: String, academicsDto: AcademicsDto): AcademicsEntity {
            return AcademicsEntity(
                to = to,
                postType = academicsDto.postType,
                name = academicsDto.name,
                description = academicsDto.description,
                year = academicsDto.year,
                isPublic = academicsDto.isPublic,
            )
        }
    }
}