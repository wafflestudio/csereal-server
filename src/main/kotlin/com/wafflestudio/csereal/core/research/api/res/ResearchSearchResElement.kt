package com.wafflestudio.csereal.core.research.api.res

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.utils.substringAroundKeyword
import com.wafflestudio.csereal.core.research.database.ResearchSearchEntity
import com.wafflestudio.csereal.core.research.type.ResearchRelatedType

data class ResearchSearchResElement(
    val id: Long,
    val language: String,
    val name: String,
    val researchType: ResearchRelatedType,
    val partialDescription: String,
    val boldStartIdx: Int,
    val boldEndIdx: Int
) {
    companion object {
        fun of(
            researchSearchEntity: ResearchSearchEntity,
            keyword: String,
            amount: Int
        ): ResearchSearchResElement =
            when {
                researchSearchEntity.research != null &&
                    researchSearchEntity.lab == null &&
                    researchSearchEntity.conferenceElement == null
                -> researchSearchEntity.research!!.let {
                    val (startIdx, partialDesc) = substringAroundKeyword(
                        keyword,
                        researchSearchEntity.content,
                        amount
                    )
                    ResearchSearchResElement(
                        id = it.id,
                        name = it.name,
                        language = it.language.let { ln -> LanguageType.makeLowercase(ln) },
                        researchType = it.postType.ofResearchRelatedType(),
                        partialDescription = partialDesc,
                        boldStartIdx = startIdx ?: 0,
                        boldEndIdx = startIdx?.plus(keyword.length) ?: 0
                    )
                }

                researchSearchEntity.lab != null &&
                    researchSearchEntity.research == null &&
                    researchSearchEntity.conferenceElement == null
                -> researchSearchEntity.lab!!.let {
                    val (startIdx, partialDesc) = substringAroundKeyword(
                        keyword,
                        researchSearchEntity.content,
                        amount
                    )
                    ResearchSearchResElement(
                        id = it.id,
                        name = it.name,
                        language = it.language.let { ln -> LanguageType.makeLowercase(ln) },
                        researchType = ResearchRelatedType.LAB,
                        partialDescription = partialDesc,
                        boldStartIdx = startIdx ?: 0,
                        boldEndIdx = startIdx?.plus(keyword.length) ?: 0
                    )
                }

                researchSearchEntity.conferenceElement != null &&
                    researchSearchEntity.research == null &&
                    researchSearchEntity.lab == null
                -> researchSearchEntity.conferenceElement!!.let {
                    val (startIdx, partialDesc) = substringAroundKeyword(
                        keyword,
                        researchSearchEntity.content,
                        amount
                    )
                    ResearchSearchResElement(
                        id = it.id,
                        name = it.name,
                        language = it.language.let { ln -> LanguageType.makeLowercase(ln) },
                        researchType = ResearchRelatedType.CONFERENCE,
                        partialDescription = partialDesc,
                        boldStartIdx = startIdx ?: 0,
                        boldEndIdx = startIdx?.plus(keyword.length) ?: 0
                    )
                }

                else -> throw CserealException.Csereal401(
                    "ResearchSearchEntity의 연결이 올바르지 않습니다."
                )
            }
    }
}
