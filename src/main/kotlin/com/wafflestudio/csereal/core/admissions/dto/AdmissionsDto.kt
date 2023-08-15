package com.wafflestudio.csereal.core.admissions.dto

import com.wafflestudio.csereal.core.admissions.database.AdmissionPostType
import com.wafflestudio.csereal.core.admissions.database.AdmissionsEntity
import java.time.LocalDateTime

data class AdmissionsDto(
    val id: Long,
    val postType: AdmissionPostType,
    val title: String,
    val description: String,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val isPublic: Boolean,
) {
    companion object {
        fun of(entity: AdmissionsEntity) : AdmissionsDto = entity.run {
            AdmissionsDto(
                id = this.id,
                postType = this.postType,
                title = this.title,
                description = this.description,
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                isPublic = this.isPublic
            )
        }
    }
}