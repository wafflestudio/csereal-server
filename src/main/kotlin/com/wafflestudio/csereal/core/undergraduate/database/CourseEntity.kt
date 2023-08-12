package com.wafflestudio.csereal.core.undergraduate.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.undergraduate.dto.CourseDto
import jakarta.persistence.Entity

@Entity(name = "course")
class CourseEntity(
    var isDeleted: Boolean = false,

    var classification: String,

    var number: String,

    var title: String,

    var credit: Int,

    var year: String,

    var courseUrl: String?,

    var description: String?
): BaseTimeEntity() {
    companion object {
        fun of(courseDto: CourseDto): CourseEntity {
            return CourseEntity(
                classification = courseDto.classification,
                number = courseDto.number,
                title = courseDto.title.replace(" ","-"),
                credit = courseDto.credit,
                year = courseDto.year,
                courseUrl = courseDto.courseUrl,
                description = courseDto.description
            )
        }
    }
}