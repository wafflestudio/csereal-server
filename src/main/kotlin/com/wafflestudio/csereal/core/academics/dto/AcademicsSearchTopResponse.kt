package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.AcademicsSearchEntity

data class AcademicsSearchTopResponse(
    val topAcademics: List<AcademicsSearchResponseElement>
) {
    companion object {
        fun of(
            topAcademics: List<AcademicsSearchEntity>
        ) = AcademicsSearchTopResponse(
            topAcademics = topAcademics.map(AcademicsSearchResponseElement::of)
        )
    }
}
