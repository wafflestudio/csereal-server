package com.wafflestudio.csereal.core.academics.api.res

import com.wafflestudio.csereal.core.academics.database.AcademicsSearchEntity

data class AcademicsSearchResBody(
    val results: List<AcademicsSearchResElement>,
    val total: Long
) {
    companion object {
        fun of(
            total: Long,
            academics: List<AcademicsSearchEntity>,
            keyword: String,
            amount: Int
        ) = AcademicsSearchResBody(
            results = academics.map {
                AcademicsSearchResElement.of(
                    it,
                    keyword,
                    amount
                )
            },
            total = total
        )
    }
}
