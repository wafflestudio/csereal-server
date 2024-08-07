package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.CourseRepository

data class GroupedCourseDto(
    val code: String,
    val credit: Int,
    val grade: Int,
    val studentType: String,
    val ko: SingleCourseDto,
    val en: SingleCourseDto
)

data class SingleCourseDto(
    val name: String,
    val description: String,
    val classification: String
)

object CourseMapper {
    fun toGroupedCourseDTO(projection: CourseRepository.CourseProjection): GroupedCourseDto {
        return GroupedCourseDto(
            code = projection.code,
            credit = projection.credit,
            grade = projection.grade,
            studentType = projection.studentType,
            ko = SingleCourseDto(
                name = projection.koName,
                description = projection.koDescription,
                classification = projection.koClassification
            ),
            en = SingleCourseDto(
                name = projection.enName,
                description = projection.enDescription,
                classification = projection.enClassification
            )
        )
    }
}
