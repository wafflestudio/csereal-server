package com.wafflestudio.csereal.core.research.event

data class LabDeletedEvent(
    val id: Long,
    val researchId: Long?,
    val professorIds: Set<Long>
)
