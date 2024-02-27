package com.wafflestudio.csereal.core.about.database

import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import com.wafflestudio.csereal.common.utils.substringAroundKeyword

data class AboutSearchElementDto private constructor(
    val id: Long,
    val language: String,
    val aboutPostType: AboutPostType,
    val name: String?,
    val partialDescription: String,
    val boldStartIndex: Int,
    val boldEndIndex: Int
) {
    companion object {
        fun of(about: AboutEntity, keyword: String, amount: Int) = about.run {
            val (boldStartIdx, partialDescription) = substringAroundKeyword(
                keyword,
                cleanTextFromHtml(description),
                amount
            )

            AboutSearchElementDto(
                id = id,
                language = LanguageType.makeLowercase(language),
                aboutPostType = postType,
                name = name,
                partialDescription = partialDescription.replace('\n', ' '),
                boldStartIndex = boldStartIdx ?: 0,
                boldEndIndex = boldStartIdx?.plus(keyword.length) ?: 0
            )
        }
    }
}
