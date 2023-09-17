package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.*

@Entity(name = "education")
class EducationEntity(
    @Column(columnDefinition = "mediumText")
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id")
    val professor: ProfessorEntity
) : BaseTimeEntity() {
    companion object {
        fun create(name: String, professor: ProfessorEntity): EducationEntity {
            val educationEntity = EducationEntity(
                name = name,
                professor = professor
            )
            professor.educations.add(educationEntity)
            return educationEntity
        }
    }
}
