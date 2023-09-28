package com.wafflestudio.csereal.core.admissions.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.admissions.dto.AdmissionsDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity(name = "admissions")
class AdmissionsEntity(
    @Enumerated(EnumType.STRING)
    val postType: AdmissionsPostType,
    val pageName: String,

    @Column(columnDefinition = "mediumText")
    val description: String
) : BaseTimeEntity() {
    companion object {
        fun of(postType: AdmissionsPostType, pageName: String, admissionsDto: AdmissionsDto): AdmissionsEntity {
            return AdmissionsEntity(
                postType = postType,
                pageName = pageName,
                description = admissionsDto.description
            )
        }
    }
}
