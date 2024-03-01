package com.wafflestudio.csereal.core.member.api.res

import com.wafflestudio.csereal.core.member.database.MemberSearchEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity

data class MemberSearchResBody(
    val members: List<MemberSearchResponseElement>,
    val total: Long
) {
    companion object {
        fun of(
            members: List<MemberSearchEntity>,
            total: Long,
            imageURLMaker: (MainImageEntity?) -> String?
        ) = MemberSearchResBody(
            members = members.map { MemberSearchResponseElement.of(it, imageURLMaker) },
            total = total
        )
    }
}
