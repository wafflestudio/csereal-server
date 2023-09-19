package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.AcademicsEntity

class GeneralStudiesPageResponse(
    val subjectChanges: List<SubjectChangesDto>,
    val description: String
) {
    companion object {
        fun of(entity: AcademicsEntity, subjectChangesEntity: List<AcademicsEntity>) = entity.run {
            GeneralStudiesPageResponse(
                subjectChanges = subjectChangesEntity.map { SubjectChangesDto.of(it) },
                description = this.description
            )
        }
    }
}
