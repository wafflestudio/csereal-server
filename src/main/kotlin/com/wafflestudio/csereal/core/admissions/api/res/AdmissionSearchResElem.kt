package com.wafflestudio.csereal.core.admissions.api.res

import com.wafflestudio.csereal.common.properties.LanguageType

data class AdmissionSearchResElem private constructor(
    val id: Long,
    val name: String,
    val mainType: String,
    val postType: String,
    val language: String,
    val partialDescription: String,
    val boldStartIndex: Int,
    val boldEndIndex: Int
) {
    companion object {
        fun of(
            admissions: AdmissionsEntity,
            keyword: String,
            amount: Int
        ) = admissions.let {
            val (boldStartIdx, partialDescription) = substringAroundKeyword(
                keyword = keyword,
                content = it.description,
                amount = amount
            )

            AdmissionSearchResElem(
                id = it.id,
                name = it.name,
                mainType = it.mainType.toJsonValue(),
                postType = it.postType.toJsonValue(),
                language = LanguageType.makeLowercase(it.language),
                partialDescription = partialDescription.replace('\n', ' '),
                boldStartIndex = boldStartIdx ?: 0,
                boldEndIndex = boldStartIdx?.plus(keyword.length) ?: 0
            )
        }
    }
}
