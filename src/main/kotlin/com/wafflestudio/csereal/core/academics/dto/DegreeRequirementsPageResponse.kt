package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.AcademicsEntity

class DegreeRequirementsPageResponse(
    val description: String,
    val yearList: List<DegreeRequirementsDto>
) {
    companion object {
        fun of(entity: AcademicsEntity, yearList: List<AcademicsEntity>) = entity.run {
            DegreeRequirementsPageResponse(
                description = this.description,
                yearList = yearList.map { DegreeRequirementsDto.of(it) }
            )
        }
    }
}
