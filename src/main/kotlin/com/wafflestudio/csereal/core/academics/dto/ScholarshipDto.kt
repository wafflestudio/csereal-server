package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.ScholarshipEntity

data class ScholarshipDto(
    val id: Long,
    val name: String,
    val description: String
) {
    companion object {
        fun of(scholarshipEntity: ScholarshipEntity): ScholarshipDto {
            return ScholarshipDto(
                id = scholarshipEntity.id,
                name = scholarshipEntity.name,
                description = scholarshipEntity.description
            )
        }
    }
}
