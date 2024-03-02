package com.wafflestudio.csereal.core.research.api.res

import com.wafflestudio.csereal.core.research.database.ResearchSearchEntity

data class ResearchSearchResBody(
    val results: List<ResearchSearchResElement>,
    val total: Long
) {
    companion object {
        fun of(
            researches: List<ResearchSearchEntity>,
            keyword: String,
            amount: Int,
            total: Long
        ) = ResearchSearchResBody(
            results = researches.map {
                ResearchSearchResElement.of(it, keyword, amount)
            },
            total = total
        )
    }
}
