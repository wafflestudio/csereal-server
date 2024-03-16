package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.AcademicsEntity

class GeneralStudiesRequirementsPageResponse(
    val overview: String,
    val subjectChanges: String,
    val generalStudies: List<GeneralStudiesDto>
) {
    companion object {
        fun of(
            overview: AcademicsEntity,
            subjectChanges: AcademicsEntity,
            generalStudies: List<AcademicsEntity>
        ): GeneralStudiesRequirementsPageResponse {
            return GeneralStudiesRequirementsPageResponse(
                overview = overview.description,
                subjectChanges = subjectChanges.description,
                generalStudies = generalStudies.map { GeneralStudiesDto.of(it) }
            )
        }
    }
}
