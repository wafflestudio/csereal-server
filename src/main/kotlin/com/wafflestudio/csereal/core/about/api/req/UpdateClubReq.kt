package com.wafflestudio.csereal.core.about.api.req

data class UpdateClubReq(
    val id: Long,
    val removeImage: Boolean,
    val ko: ClubReqBody,
    val en: ClubReqBody
)
