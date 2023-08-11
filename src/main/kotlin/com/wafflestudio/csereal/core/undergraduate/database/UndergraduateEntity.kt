package com.wafflestudio.csereal.core.undergraduate.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.undergraduate.dto.UndergraduateDto
import jakarta.persistence.Entity

@Entity(name = "undergraduate")
class UndergraduateEntity(
    var postType: String,

    var title: String,

    var description: String,

    var year: Int?,

    var isPublic: Boolean,

): BaseTimeEntity() {
    companion object {
        fun of(postType: String, undergraduateDto: UndergraduateDto): UndergraduateEntity {
            return UndergraduateEntity(
                postType = postType,
                title = undergraduateDto.title,
                description = undergraduateDto.description,
                year = undergraduateDto.year,
                isPublic = undergraduateDto.isPublic,
            )
        }
    }
}