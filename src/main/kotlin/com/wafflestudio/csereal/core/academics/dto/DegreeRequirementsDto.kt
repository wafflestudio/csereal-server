package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.AcademicsEntity

class DegreeRequirementsDto(
    val year: Int,
    val description: String
) {
    companion object {
        fun of(entity: AcademicsEntity) = entity.run {
            DegreeRequirementsDto(
                year = this.year!!,
                description = this.description
            )
        }
    }
}
