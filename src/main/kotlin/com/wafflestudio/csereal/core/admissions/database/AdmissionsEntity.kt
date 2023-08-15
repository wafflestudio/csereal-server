package com.wafflestudio.csereal.core.admissions.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.admissions.dto.AdmissionsDto
import jakarta.persistence.Entity

@Entity(name = "admissions")
class AdmissionsEntity(
    val postType: String,
    val admissionsType: String,
    val title: String,
    val description: String,
    val isPublic: Boolean,
): BaseTimeEntity() {
    companion object {
        fun of(postType: String, admissionsType: String, admissionsDto: AdmissionsDto) : AdmissionsEntity {
            return AdmissionsEntity(
                postType = postType,
                admissionsType = admissionsType,
                title = admissionsDto.title,
                description = admissionsDto.description,
                isPublic = admissionsDto.isPublic
            )
        }
    }
}