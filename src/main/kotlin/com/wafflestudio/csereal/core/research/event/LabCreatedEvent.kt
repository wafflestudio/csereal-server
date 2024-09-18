package com.wafflestudio.csereal.core.research.event

data class LabCreatedEvent(
    val id: Long,
    val researchId: Long?,
    val professorIds: Set<Long>
)
