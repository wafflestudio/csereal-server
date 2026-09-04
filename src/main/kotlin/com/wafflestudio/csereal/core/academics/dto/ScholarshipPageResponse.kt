package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.AcademicsEntity
import com.wafflestudio.csereal.core.academics.database.ScholarshipTranslationEntity

class ScholarshipPageResponse(
    val description: String,
    val scholarships: List<SimpleScholarshipDto>
) {
    companion object {
        fun of(
            page: AcademicsEntity,
            translations: List<ScholarshipTranslationEntity>
        ): ScholarshipPageResponse = ScholarshipPageResponse(
            description = page.description,
            scholarships = translations.map { SimpleScholarshipDto.of(it) }
        )
    }
}
