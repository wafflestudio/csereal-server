package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.AcademicsEntity

class GeneralStudiesRequirementsPageResponse(
    val overview: String,
    val generalStudies: List<GeneralStudiesDto>
) {
    companion object {
        fun of(
            overview: AcademicsEntity,
            generalStudies: List<AcademicsEntity>
        ): GeneralStudiesRequirementsPageResponse {
            return GeneralStudiesRequirementsPageResponse(
                overview = overview.description,
                generalStudies = generalStudies.map { GeneralStudiesDto.of(it) }
            )
        }
    }
}
