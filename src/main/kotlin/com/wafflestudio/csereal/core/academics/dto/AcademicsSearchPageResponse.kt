package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.AcademicsSearchEntity

data class AcademicsSearchPageResponse(
    val academics: List<AcademicsSearchResponseElement>,
    val total: Long
) {
    companion object {
        fun of(
            academics: List<AcademicsSearchEntity>,
            total: Long
        ) = AcademicsSearchPageResponse(
            academics = academics.map(AcademicsSearchResponseElement::of),
            total = total
        )
    }
}
