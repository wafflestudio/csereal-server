package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.*

@Entity(name = "career")
class CareerEntity(
    @Column(columnDefinition = "mediumText")
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id")
    val professor: ProfessorEntity
) : BaseTimeEntity() {
    companion object {

        fun create(name: String, professor: ProfessorEntity): CareerEntity {
            val careerEntity = CareerEntity(
                name = name,
                professor = professor
            )
            professor.careers.add(careerEntity)
            return careerEntity
        }
    }
}
