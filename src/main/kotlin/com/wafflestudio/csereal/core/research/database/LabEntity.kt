package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.member.database.ProfessorEntity
import jakarta.persistence.*

@Entity(name = "lab")
class LabEntity(
    
    val name: String,

    @OneToMany(mappedBy = "lab")
    val professors: MutableSet<ProfessorEntity> = mutableSetOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_id")
    val research: ResearchEntity
) : BaseTimeEntity() {
    companion object {
        fun create(name: String, research: ResearchEntity): LabEntity {
            val labEntity = LabEntity(
                name = name,
                research = research
            )
            research.labs.add(labEntity)
            return labEntity
        }
    }
}
