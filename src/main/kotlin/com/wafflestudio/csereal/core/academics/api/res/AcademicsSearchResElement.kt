package com.wafflestudio.csereal.core.academics.api.res

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.utils.substringAroundKeyword
import com.wafflestudio.csereal.core.academics.database.AcademicsPostType
import com.wafflestudio.csereal.core.academics.database.AcademicsSearchEntity
import com.wafflestudio.csereal.core.academics.database.AcademicsSearchType
import com.wafflestudio.csereal.core.academics.database.AcademicsStudentType

data class AcademicsSearchResElement(
    val id: Long,
    val language: String,
    val name: String,
    val postType: AcademicsSearchType,
    val studentType: AcademicsStudentType,
    val academicType: AcademicsPostType? = null,
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
                    academicsSearch.academics!!.let {
                        AcademicsSearchResElement(
                            id = it.id,
                            name = it.name,
                            language = it.language.let { lan ->
                                LanguageType.makeLowercase(lan)
                            },
                            postType = AcademicsSearchType.ACADEMICS,
                            academicType = it.postType,
                            studentType = it.studentType,
                            partialDescription = partialDescription.replace("\n", " "),
                            boldStartIndex = startIdx ?: 0,
                            boldEndIndex = startIdx?.plus(keyword.length) ?: 0
                        )
                    }
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
                        postType = AcademicsSearchType.COURSE,
                        studentType = academicsSearch.course!!.studentType,
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
                        // id 는 장학금 자체(부모)의 id — 상세 URL 이 그 id 를 쓴다.
                        id = academicsSearch.scholarship!!.scholarship.id,
                        name = academicsSearch.scholarship!!.name,
                        language = academicsSearch.scholarship!!.language.let {
                            LanguageType.makeLowercase(it)
                        },
                        postType = AcademicsSearchType.SCHOLARSHIP,
                        studentType = academicsSearch.scholarship!!.scholarship.studentType,
                        partialDescription = partialDescription.replace("\n", " "),
                        boldStartIndex = startIdx ?: 0,
                        boldEndIndex = startIdx?.plus(keyword.length) ?: 0
                    )
                }

                else -> throw CserealException(ErrorCode.SEARCH_INDEX_BROKEN)
            }
        }
    }
}
