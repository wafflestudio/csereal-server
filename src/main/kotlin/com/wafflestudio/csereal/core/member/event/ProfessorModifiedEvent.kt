package com.wafflestudio.csereal.core.member.event

import com.wafflestudio.csereal.core.member.database.ProfessorEntity

data class ProfessorModifiedEvent(
    val id: Long,
    val beforeLabId: Long?,
    val afterLabId: Long?
) {
    companion object {
        fun of(updatedProfessor: ProfessorEntity, beforeLabId: Long?) = ProfessorModifiedEvent(
            id = updatedProfessor.id,
            beforeLabId,
            afterLabId = updatedProfessor.lab?.id
        )
    }
}
