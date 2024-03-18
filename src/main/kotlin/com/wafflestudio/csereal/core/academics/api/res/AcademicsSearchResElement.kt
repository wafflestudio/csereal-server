package com.wafflestudio.csereal.core.academics.api.res

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.properties.LanguageType
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
                        id = academicsSearch.scholarship!!.id,
                        name = academicsSearch.scholarship!!.name,
                        language = academicsSearch.scholarship!!.language.let {
                            LanguageType.makeLowercase(it)
                        },
                        postType = AcademicsSearchType.SCHOLARSHIP,
                        studentType = academicsSearch.scholarship!!.studentType,
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
