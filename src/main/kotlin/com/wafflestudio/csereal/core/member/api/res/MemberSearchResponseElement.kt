package com.wafflestudio.csereal.core.member.api.res

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.database.MemberSearchEntity
import com.wafflestudio.csereal.core.member.type.MemberType
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity

data class MemberSearchResponseElement(
    val id: Long,
    val language: String,
    val name: String,
    val academicRankOrRole: String,
    val imageURL: String?,
    val memberType: MemberType
) {
    companion object {
        fun of(
            memberSearch: MemberSearchEntity,
            imageURLMaker: (MainImageEntity?) -> String?
        ): MemberSearchResponseElement =
            when {
                memberSearch.professor != null && memberSearch.staff == null ->
                    MemberSearchResponseElement(
                        id = memberSearch.professor!!.id,
                        language = memberSearch.language.let { LanguageType.makeLowercase(it) },
                        name = memberSearch.professor!!.name,
                        academicRankOrRole = memberSearch.professor!!.academicRank,
                        imageURL = imageURLMaker(memberSearch.professor!!.mainImage),
                        memberType = MemberType.PROFESSOR
                    )
                memberSearch.professor == null && memberSearch.staff != null ->
                    MemberSearchResponseElement(
                        id = memberSearch.staff!!.id,
                        language = memberSearch.language.let { LanguageType.makeLowercase(it) },
                        name = memberSearch.staff!!.name,
                        academicRankOrRole = memberSearch.staff!!.role,
                        imageURL = imageURLMaker(memberSearch.staff!!.mainImage),
                        memberType = MemberType.STAFF
                    )
                else -> throw CserealException.Csereal401(
                    "MemberSearchEntity는 professor 혹은 staff 중 하나와만 연결되어있어야 합니다."
                )
            }
    }
}
