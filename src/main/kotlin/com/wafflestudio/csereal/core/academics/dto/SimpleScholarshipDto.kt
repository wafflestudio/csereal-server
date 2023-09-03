package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.ScholarshipEntity

data class SimpleScholarshipDto(
    val id: Long,
    val name: String
) {
    companion object {
        fun of(scholarshipEntity: ScholarshipEntity): SimpleScholarshipDto {
            return SimpleScholarshipDto(
                id = scholarshipEntity.id,
                name = scholarshipEntity.name
            )
        }
    }
}
