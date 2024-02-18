package com.wafflestudio.csereal.core.admissions.api.res

import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.admissions.database.AdmissionsEntity

data class AdmissionSearchResBody(
    val admissions: List<AdmissionSearchResElem>
)

data class AdmissionSearchResElem(
    val id: Long,
    val name: String,
    val mainType: String,
    val postType: String,
    val language: String
) {
    companion object {
        fun of(
            admissions: AdmissionsEntity
        ) = AdmissionSearchResElem(
            id = admissions.id,
            name = admissions.name,
            mainType = admissions.mainType.toJsonValue(),
            postType = admissions.postType.toJsonValue(),
            language = LanguageType.makeLowercase(admissions.language)
        )
    }
}
