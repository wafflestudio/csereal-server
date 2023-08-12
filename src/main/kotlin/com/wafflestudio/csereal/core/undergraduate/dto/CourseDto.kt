package com.wafflestudio.csereal.core.undergraduate.dto

import com.wafflestudio.csereal.core.undergraduate.database.CourseEntity

data class CourseDto(
    val id: Long,
    val classification: String,
    val number: String,
    val title: String,
    val credit: Int,
    val year: String,
    val courseUrl: String?,
    val description: String?
) {
    companion object {
        fun of(entity: CourseEntity): CourseDto = entity.run {
            CourseDto(
                id = this.id,
                classification = this.classification,
                number = this.number,
                title = this.title,
                credit = this.credit,
                year = this.year,
                courseUrl = this.courseUrl,
                description = this.description,
            )
        }
    }
}