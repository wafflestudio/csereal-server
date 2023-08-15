package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.member.database.ProfessorEntity
import com.wafflestudio.csereal.core.research.dto.LabDto
import jakarta.persistence.*

@Entity(name = "lab")
class LabEntity(
    
    val name: String,

    val initial: String?,

    val researchGroup: String,

    val labs: String?,

    val phone: String?,

    val fax: String?,

    val website: String?,

    val description: String?,

    val isPublic: Boolean,

    @OneToMany(mappedBy = "lab")
    val professors: MutableSet<ProfessorEntity> = mutableSetOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_id")
    var research: ResearchEntity
) : BaseTimeEntity() {
    companion object {
        fun of(researchGroup: ResearchEntity, labDto: LabDto) : LabEntity {
            return LabEntity(
                name = labDto.name,
                initial = labDto.initial,
                labs = labDto.labs,
                phone = labDto.phone,
                fax = labDto.fax,
                website = labDto.website,
                description = labDto.description,
                isPublic = labDto.isPublic,
                research = researchGroup,
            )
        }
    }
}
