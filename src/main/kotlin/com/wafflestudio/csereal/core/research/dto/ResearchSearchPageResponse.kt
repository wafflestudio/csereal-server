package com.wafflestudio.csereal.core.research.dto

import com.wafflestudio.csereal.core.research.database.ResearchSearchEntity

data class ResearchSearchPageResponse(
    val researches: List<ResearchSearchResponseElement>,
    val total: Long
) {
    companion object {
        fun of(
            researches: List<ResearchSearchEntity>,
            total: Long
        ) = ResearchSearchPageResponse(
            researches = researches.map(ResearchSearchResponseElement::of),
            total = total
        )
    }
}
