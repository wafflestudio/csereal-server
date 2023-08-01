package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity(name = "research_area")
class ResearchAreaEntity(
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id")
    val professor: ProfessorEntity
) : BaseTimeEntity() {
    companion object {
        fun create(name: String, professor: ProfessorEntity): ResearchAreaEntity {
            val researchArea = ResearchAreaEntity(
                name = name,
                professor = professor
            )
            professor.researchAreas.add(researchArea)
            return researchArea
        }
    }
}
