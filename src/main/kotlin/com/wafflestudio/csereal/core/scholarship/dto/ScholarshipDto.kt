package com.wafflestudio.csereal.core.scholarship.dto

import com.wafflestudio.csereal.core.scholarship.database.ScholarshipEntity

data class ScholarshipDto(
    val id: Long,
    val title: String,
    val description: String
) {
    companion object {
        fun of(scholarshipEntity: ScholarshipEntity): ScholarshipDto {
            return ScholarshipDto(
                id = scholarshipEntity.id,
                title = scholarshipEntity.title,
                description = scholarshipEntity.description
            )
        }
    }
}
