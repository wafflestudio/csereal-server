package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.AcademicsEntity

class GeneralStudiesDto(
    val id: Long,
    val year: Int,
    val description: String
) {
    companion object {
        fun of(academicsEntity: AcademicsEntity): GeneralStudiesDto {
            return GeneralStudiesDto(
                id = academicsEntity.id,
                year = academicsEntity.year!!,
                description = academicsEntity.description
            )
        }
    }
}
