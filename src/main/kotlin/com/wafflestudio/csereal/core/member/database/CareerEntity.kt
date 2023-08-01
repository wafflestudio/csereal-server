package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.member.dto.CareerDto
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity(name = "career")
class CareerEntity(
    val duration: String,
    val name: String,
    val workplace: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id")
    val professor: ProfessorEntity
) : BaseTimeEntity() {
    companion object {
        fun create(career: CareerDto, professor: ProfessorEntity): CareerEntity {
            val careerEntity = CareerEntity(
                duration = career.duration,
                name = career.name,
                workplace = career.workplace,
                professor = professor
            )
            professor.careers.add(careerEntity)
            return careerEntity
        }
    }
}
