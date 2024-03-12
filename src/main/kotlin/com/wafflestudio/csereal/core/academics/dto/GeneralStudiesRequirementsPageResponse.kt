package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.AcademicsEntity

class GeneralStudiesRequirementsPageResponse(
    val description: String
) {
    companion object {
        fun of(entity: AcademicsEntity): GeneralStudiesRequirementsPageResponse = entity.run {
            GeneralStudiesRequirementsPageResponse(
                description = this.description
            )
        }
    }
}
