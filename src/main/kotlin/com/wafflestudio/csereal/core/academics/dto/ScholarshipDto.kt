package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.academics.database.ScholarshipEntity

data class ScholarshipDto(
    val id: Long,
    val language: String,
    val name: String,
    val description: String
) {
    companion object {
        fun of(scholarshipEntity: ScholarshipEntity): ScholarshipDto {
            return ScholarshipDto(
                id = scholarshipEntity.id,
                language = LanguageType.makeLowercase(scholarshipEntity.language),
                name = scholarshipEntity.name,
                description = scholarshipEntity.description
            )
        }
    }
}
