package com.wafflestudio.csereal.core.academics.api.res

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.common.utils.substringAroundKeyword
import com.wafflestudio.csereal.core.academics.database.AcademicsSearchEntity
import com.wafflestudio.csereal.core.academics.database.AcademicsSearchType

data class AcademicsSearchResElement(
    val id: Long,
    val language: String,
    val name: String,
    val academicsType: AcademicsSearchType,
    val partialDescription: String,
    val boldStartIndex: Int,
    val boldEndIndex: Int
) {
    companion object {
        fun of(
            academicsSearch: AcademicsSearchEntity,
            keyword: String,
            amount: Int
        ): AcademicsSearchResElement {
            return when {
                academicsSearch.academics != null &&
                    academicsSearch.course == null &&
                    academicsSearch.scholarship == null -> {
                    val (startIdx, partialDescription) = substringAroundKeyword(
                        keyword,
                        academicsSearch.content,
                        amount
                    )
                    AcademicsSearchResElement(
                        id = academicsSearch.academics!!.id,
                        name = academicsSearch.academics!!.name,
                        language = academicsSearch.academics!!.language.let {
                            LanguageType.makeLowercase(it)
                        },
                        academicsType = AcademicsSearchType.ACADEMICS,
                        partialDescription = partialDescription.replace("\n", " "),
                        boldStartIndex = startIdx ?: 0,
                        boldEndIndex = startIdx?.plus(keyword.length) ?: 0
                    )
                }

                academicsSearch.academics == null &&
                    academicsSearch.course != null &&
                    academicsSearch.scholarship == null -> {
                    val (startIdx, partialDescription) = substringAroundKeyword(
                        keyword,
                        academicsSearch.content,
                        amount
                    )
                    AcademicsSearchResElement(
                        id = academicsSearch.course!!.id,
                        name = academicsSearch.course!!.name,
                        language = academicsSearch.course!!.language.let {
                            LanguageType.makeLowercase(it)
                        },
                        academicsType = AcademicsSearchType.COURSE,
                        partialDescription = partialDescription.replace("\n", " "),
                        boldStartIndex = startIdx ?: 0,
                        boldEndIndex = startIdx?.plus(keyword.length) ?: 0
                    )
                }

                academicsSearch.academics == null &&
                    academicsSearch.course == null &&
                    academicsSearch.scholarship != null -> {
                    val (startIdx, partialDescription) = substringAroundKeyword(
                        keyword,
                        academicsSearch.content,
                        amount
                    )
                    AcademicsSearchResElement(
                        id = academicsSearch.scholarship!!.id,
                        name = academicsSearch.scholarship!!.name,
                        language = academicsSearch.scholarship!!.language.let {
                            LanguageType.makeLowercase(it)
                        },
                        academicsType = AcademicsSearchType.SCHOLARSHIP,
                        partialDescription = partialDescription.replace("\n", " "),
                        boldStartIndex = startIdx ?: 0,
                        boldEndIndex = startIdx?.plus(keyword.length) ?: 0
                    )
                }

                else -> throw CserealException.Csereal401("AcademicsSearchEntity의 연결이 올바르지 않습니다.")
            }
        }
    }
}
