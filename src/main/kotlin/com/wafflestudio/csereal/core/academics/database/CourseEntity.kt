package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.academics.dto.CourseDto
import jakarta.persistence.Entity

@Entity(name = "course")
class CourseEntity(
    var isDeleted: Boolean = false,

    var studentType: AcademicsStudentType,

    var classification: String,

    var number: String,

    var name: String,

    var credit: Int,

    var year: String,

    var courseURL: String?,

    var description: String?
): BaseTimeEntity() {
    companion object {
        fun of(studentType: AcademicsStudentType, courseDto: CourseDto): CourseEntity {
            return CourseEntity(
                studentType = studentType,
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