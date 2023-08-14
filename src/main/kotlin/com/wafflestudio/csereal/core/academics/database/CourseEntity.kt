package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.academics.dto.CourseDto
import jakarta.persistence.Entity

@Entity(name = "course")
class CourseEntity(
    var isDeleted: Boolean = false,

    var to: String,

    var classification: String,

    var number: String,

    var name: String,

    var credit: Int,

    var year: String,

    var courseURL: String?,

    var description: String?
): BaseTimeEntity() {
    companion object {
        fun of(to: String, courseDto: CourseDto): CourseEntity {
            return CourseEntity(
                to = to,
                classification = courseDto.classification,
                number = courseDto.number,
                name = courseDto.name.replace(" ","-"),
                credit = courseDto.credit,
                year = courseDto.year,
                courseURL = courseDto.courseURL,
                description = courseDto.description
            )
        }
    }
}