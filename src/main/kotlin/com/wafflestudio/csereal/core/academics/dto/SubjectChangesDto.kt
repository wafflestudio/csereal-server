package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.AcademicsEntity

class SubjectChangesDto(
    val time: String,
    val description: String,
) {
    companion object {
        fun of(entity: AcademicsEntity) = entity.run {
            SubjectChangesDto(
                time = this.time!!,
                description = this.description
            )
        }
    }
}