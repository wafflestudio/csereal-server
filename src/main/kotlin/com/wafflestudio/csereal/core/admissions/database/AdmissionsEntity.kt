package com.wafflestudio.csereal.core.admissions.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.admissions.dto.AdmissionsDto
import jakarta.persistence.Entity

@Entity(name = "admissions")
class AdmissionsEntity(
    val to: String,
    val postType: String,
    val title: String,
    val description: String,
    val isPublic: Boolean,
): BaseTimeEntity() {
    companion object {
        fun of(admissionsDto: AdmissionsDto) : AdmissionsEntity {
            return AdmissionsEntity(
                to = admissionsDto.to,
                postType = admissionsDto.postType,
                title = admissionsDto.title,
                description = admissionsDto.description,
                isPublic = admissionsDto.isPublic
            )
        }
    }
}