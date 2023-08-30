package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.AcademicsEntity
import com.wafflestudio.csereal.core.scholarship.database.ScholarshipEntity
import com.wafflestudio.csereal.core.scholarship.dto.SimpleScholarshipDto
import java.time.LocalDateTime

class ScholarshipPageResponse(
    val id: Long,
    val name: String,
    val description: String,
    val scholarships: List<SimpleScholarshipDto>
) {
    companion object {
        fun of(scholarship: AcademicsEntity, scholarships: List<ScholarshipEntity>): ScholarshipPageResponse {
            return ScholarshipPageResponse(
                id = scholarship.id,
                name = scholarship.name,
                description = scholarship.description,
                scholarships = scholarships.map { SimpleScholarshipDto.of(it) }
            )
        }
    }
}
