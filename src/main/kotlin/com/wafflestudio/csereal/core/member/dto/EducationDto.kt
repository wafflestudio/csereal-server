package com.wafflestudio.csereal.core.member.dto

import com.wafflestudio.csereal.core.member.database.Degree
import com.wafflestudio.csereal.core.member.database.EducationEntity

data class EducationDto(
    val university: String,
    val major: String,
    val degree: Degree,
    val year: Int
) {
    companion object {
        fun of(educationEntity: EducationEntity): EducationDto {
            return EducationDto(
                university = educationEntity.university,
                major = educationEntity.major,
                degree = educationEntity.degree,
                year = educationEntity.year
            )
        }
    }
}
