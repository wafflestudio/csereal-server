package com.wafflestudio.csereal.core.admissions.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Entity

@Entity(name = "admissions")
class AdmissionsEntity(
    val admissionsType: String,
    val title: String,
    val description: String,
    val isPublic: Boolean,
): BaseTimeEntity() {
    companion object {
        fun of(admissionsType: String, admissionsDto: AdmissionsDto) : AdmissionsEntity {
            return AdmissionsEntity(
                admissionsType = admissionsType,
                title = admissionsDto.title,
                description = admissionsDto.description,
                isPublic = admissionsDto.isPublic
            )
        }
    }
}