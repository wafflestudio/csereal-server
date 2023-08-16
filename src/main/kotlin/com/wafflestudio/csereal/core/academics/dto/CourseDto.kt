package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.CourseEntity

data class CourseDto(
    val id: Long,
    val classification: String,
    val number: String,
    val name: String,
    val credit: Int,
    val year: String,
    val courseURL: String?,
    val description: String?
) {
    companion object {
        fun of(entity: CourseEntity): CourseDto = entity.run {
            CourseDto(
                id = this.id,
                classification = this.classification,
                number = this.number,
                name = this.name,
                credit = this.credit,
                year = this.year,
                courseURL = this.courseURL,
                description = this.description,
            )
        }
    }
}