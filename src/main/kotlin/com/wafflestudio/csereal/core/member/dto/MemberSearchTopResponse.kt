package com.wafflestudio.csereal.core.member.dto

import com.wafflestudio.csereal.core.member.database.MemberSearchEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity

data class MemberSearchTopResponse(
        val topMembers: List<MemberSearchResponseElement>
) {
    companion object {
        fun of(
                topMembers: List<MemberSearchEntity>,
                imageURLMaker: (MainImageEntity?) -> String?
        ) = MemberSearchTopResponse(
                topMembers = topMembers.map {
                    MemberSearchResponseElement.of(it, imageURLMaker)
                }
        )
    }
}