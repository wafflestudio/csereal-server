package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.academics.database.CourseEntity

data class CourseDto(
    val id: Long,
    val language: String,
    val classification: String,
    val code: String,
    val name: String,
    val credit: Int,
    val grade: Int,
    val description: String?
) {
    companion object {
        fun of(entity: CourseEntity): CourseDto = entity.run {
            CourseDto(
                id = this.id,
                language = LanguageType.makeLowercase(this.language),
                classification = this.classification,
                code = this.code,
                name = this.name,
                credit = this.credit,
                grade = this.grade,
                description = this.description
            )
        }
    }
}
