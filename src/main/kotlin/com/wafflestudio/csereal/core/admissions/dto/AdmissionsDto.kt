package com.wafflestudio.csereal.core.admissions.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.admissions.database.AdmissionsEntity
import java.time.LocalDateTime

data class AdmissionsDto(
    val id: Long,
    val name: String,
    val mainType: String,
    val postType: String,
    val language: String,
    val description: String,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {
    companion object {
        fun of(entity: AdmissionsEntity): AdmissionsDto = entity.run {
            AdmissionsDto(
                id = this.id,
                name = this.name,
                mainType = this.mainType.toValue(),
                postType = this.postType.toValue(),
                language = LanguageType.makeLowercase(this.language),
                description = this.description,
                createdAt = this.createdAt!!,
                modifiedAt = this.modifiedAt!!
            )
        }
    }
}
