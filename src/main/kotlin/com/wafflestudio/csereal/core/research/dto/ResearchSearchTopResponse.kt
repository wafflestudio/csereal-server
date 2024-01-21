package com.wafflestudio.csereal.core.research.dto

import com.wafflestudio.csereal.core.research.database.ResearchSearchEntity

data class ResearchSearchTopResponse(
    val topResearches: List<ResearchSearchResponseElement>
) {
    companion object {
        fun of(
            topResearches: List<ResearchSearchEntity>
        ) = ResearchSearchTopResponse(
            topResearches = topResearches.map(ResearchSearchResponseElement::of)
        )
    }
}
