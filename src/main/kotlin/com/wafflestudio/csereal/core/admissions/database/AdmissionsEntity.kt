package com.wafflestudio.csereal.core.admissions.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
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
    var name: String,

    @Enumerated(EnumType.STRING)
    val language: LanguageType,

    @Enumerated(EnumType.STRING)
    val mainType: AdmissionsMainType,

    @Enumerated(EnumType.STRING)
    val postType: AdmissionsPostType,

    @Column(columnDefinition = "mediumText")
    val description: String,

    @Column(nullable = false, columnDefinition = "mediumText")
    var searchContent: String
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
            language = LanguageType.makeStringToLanguageType(admissionsDto.language),
            searchContent = createSearchContent(
                name = name,
                mainType = mainType,
                postType = postType,
                language = LanguageType.makeStringToLanguageType(admissionsDto.language),
                description = admissionsDto.description
            )
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
            language = LanguageType.makeStringToLanguageType(req.language),
            searchContent = createSearchContent(
                name = req.name,
                mainType = mainType,
                postType = postType,
                language = LanguageType.makeStringToLanguageType(req.language),
                description = req.description
            )
        )

        fun createSearchContent(
            name: String,
            mainType: AdmissionsMainType,
            postType: AdmissionsPostType,
            language: LanguageType,
            description: String
        ) = StringBuilder().apply {
            appendLine(name)
            appendLine(mainType.getLanguageValue(language))
            appendLine(postType.getLanguageValue(language))
            appendLine(cleanTextFromHtml(description))
        }.toString()
    }
}
