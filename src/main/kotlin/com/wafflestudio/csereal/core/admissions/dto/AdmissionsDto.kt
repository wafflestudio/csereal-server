package com.wafflestudio.csereal.core.admissions.dto

import com.wafflestudio.csereal.core.admissions.database.AdmissionsEntity
import java.time.LocalDateTime

data class AdmissionsDto(
    val id: Long,
    val description: String,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
) {
    companion object {
        fun of(entity: AdmissionsEntity) : AdmissionsDto = entity.run {
            AdmissionsDto(
                id = this.id,
                description = this.description,
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
            )
        }
    }
}