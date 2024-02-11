package com.wafflestudio.csereal.core.admissions.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.admissions.api.req.AdmissionReqBody
import com.wafflestudio.csereal.core.admissions.dto.AdmissionsDto
import com.wafflestudio.csereal.core.admissions.type.AdmissionsMainType
import com.wafflestudio.csereal.core.admissions.type.AdmissionsPostType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity(name = "admissions")
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["language", "mainType", "postType"])
    ]
)
class AdmissionsEntity(
    val name: String,

    @Enumerated(EnumType.STRING)
    val language: LanguageType,

    @Enumerated(EnumType.STRING)
    val mainType: AdmissionsMainType,

    @Enumerated(EnumType.STRING)
    val postType: AdmissionsPostType,

    @Column(columnDefinition = "mediumText")
    val description: String
) : BaseTimeEntity() {
    companion object {
        fun of(
            mainType: AdmissionsMainType,
            postType: AdmissionsPostType,
            name: String,
            admissionsDto: AdmissionsDto
        ) = AdmissionsEntity(
            mainType = mainType,
            postType = postType,
            name = name,
            description = admissionsDto.description,
            language = LanguageType.makeStringToLanguageType(admissionsDto.language)
        )

        fun of(
            mainType: AdmissionsMainType,
            postType: AdmissionsPostType,
            req: AdmissionReqBody
        ) = AdmissionsEntity(
            mainType = mainType,
            postType = postType,
            name = req.name!!,
            description = req.description!!,
            language = LanguageType.makeStringToLanguageType(req.language)
        )
    }
}
