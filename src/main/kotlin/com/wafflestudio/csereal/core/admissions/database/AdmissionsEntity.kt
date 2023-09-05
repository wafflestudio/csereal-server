package com.wafflestudio.csereal.core.admissions.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.admissions.dto.AdmissionsDto
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity(name = "admissions")
class AdmissionsEntity(
    @Enumerated(EnumType.STRING)
    val postType: AdmissionsPostType,
    val pageName: String,
    val description: String,
): BaseTimeEntity() {
    companion object {
        fun of(postType: AdmissionsPostType, admissionsDto: AdmissionsDto) : AdmissionsEntity {
            return AdmissionsEntity(
                postType = postType,
                pageName = admissionsDto.pageName,
                description = admissionsDto.description,
            )
        }
    }
}