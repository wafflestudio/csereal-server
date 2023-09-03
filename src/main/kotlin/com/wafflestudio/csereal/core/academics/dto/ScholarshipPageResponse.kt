package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.AcademicsEntity
import com.wafflestudio.csereal.core.academics.database.ScholarshipEntity

class ScholarshipPageResponse(
    val description: String,
    val scholarships: List<SimpleScholarshipDto>
) {
    companion object {
        fun of(scholarship: AcademicsEntity, scholarships: List<ScholarshipEntity>): ScholarshipPageResponse {
            return ScholarshipPageResponse(
                description = scholarship.description,
                scholarships = scholarships.map { SimpleScholarshipDto.of(it) }
            )
        }
    }
}
