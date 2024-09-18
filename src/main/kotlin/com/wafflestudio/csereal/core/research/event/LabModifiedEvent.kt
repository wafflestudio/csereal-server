package com.wafflestudio.csereal.core.research.event

data class LabModifiedEvent(
    val id: Long,
    val researchIdModified: Pair<Long?, Long?>,
    val professorIdsModified: Pair<Set<Long>, Set<Long>>
)
