package com.wafflestudio.csereal.core.admissions.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.admissions.dto.AdmissionsDto
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity(name = "admissions")
class AdmissionsEntity(
    @Enumerated(EnumType.STRING)
    var studentType: StudentType,
    @Enumerated(EnumType.STRING)
    val postType: AdmissionPostType,
    val title: String,
    val description: String,
    val isPublic: Boolean,
): BaseTimeEntity() {
    companion object {
        fun of(studentType: StudentType, admissionsDto: AdmissionsDto) : AdmissionsEntity {
            return AdmissionsEntity(
                studentType = studentType,
                postType = admissionsDto.postType,
                title = admissionsDto.title,
                description = admissionsDto.description,
                isPublic = admissionsDto.isPublic
            )
        }
    }
}