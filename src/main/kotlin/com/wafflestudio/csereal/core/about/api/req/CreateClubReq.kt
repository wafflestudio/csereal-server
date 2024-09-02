package com.wafflestudio.csereal.core.about.api.req

data class CreateClubReq(
    val ko: ClubReqBody,
    val en: ClubReqBody
)

data class ClubReqBody(
    val name: String,
    val description: String
)
