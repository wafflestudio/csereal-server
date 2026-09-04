package com.wafflestudio.csereal.core.about.api.res

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import com.wafflestudio.csereal.common.utils.substringAroundKeyword
import com.wafflestudio.csereal.core.about.database.AboutPostType
import com.wafflestudio.csereal.core.about.database.AboutTranslationEntity

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
        fun of(translation: AboutTranslationEntity, keyword: String, amount: Int) = translation.run {
            val (boldStartIdx, partialDescription) = substringAroundKeyword(
                keyword,
                cleanTextFromHtml(description),
                amount
            )

            AboutSearchElementDto(
                // 콘텐츠 자체(부모)의 id — 검색 결과 링크가 이 id 로 간다.
                id = about.id,
                language = LanguageType.makeLowercase(language),
                aboutPostType = about.postType,
                name = name,
                partialDescription = partialDescription.replace('\n', ' '),
                boldStartIndex = boldStartIdx ?: 0,
                boldEndIndex = boldStartIdx?.plus(keyword.length) ?: 0
            )
        }
    }
}
