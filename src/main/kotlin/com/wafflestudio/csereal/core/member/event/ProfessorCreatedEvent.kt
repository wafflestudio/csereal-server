package com.wafflestudio.csereal.core.member.event

import com.wafflestudio.csereal.core.member.database.ProfessorEntity

data class ProfessorCreatedEvent(
    val id: Long,
    val labId: Long?
) {
    companion object {
        fun of(professor: ProfessorEntity) = ProfessorCreatedEvent(
            id = professor.id,
            labId = professor.lab?.id
        )
    }
}
