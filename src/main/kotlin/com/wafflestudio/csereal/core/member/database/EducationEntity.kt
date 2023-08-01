package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.member.dto.EducationDto
import jakarta.persistence.*

@Entity(name = "education")
class EducationEntity(
    val university: String,
    val major: String,

    @Enumerated(EnumType.STRING)
    val degree: Degree,

    val year: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id")
    val professor: ProfessorEntity
) : BaseTimeEntity() {
    companion object {
        fun create(education: EducationDto, professor: ProfessorEntity): EducationEntity {
            val educationEntity = EducationEntity(
                university = education.university,
                major = education.major,
                degree = education.degree,
                year = education.year,
                professor = professor
            )
            professor.educations.add(educationEntity)
            return educationEntity
        }
    }
}

enum class Degree {
    Bachelor, Master, PhD
}
