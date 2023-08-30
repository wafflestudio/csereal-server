package com.wafflestudio.csereal.core.scholarship.dto

import com.wafflestudio.csereal.core.scholarship.database.ScholarshipEntity

data class SimpleScholarshipDto(
    val id: Long,
    val title: String
) {
    companion object {
        fun of(scholarshipEntity: ScholarshipEntity): SimpleScholarshipDto {
            return SimpleScholarshipDto(
                id = scholarshipEntity.id,
                title = scholarshipEntity.title
            )
        }
    }
}
