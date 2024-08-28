package com.wafflestudio.csereal.core.about.api.req

data class CreateClubReq(
    val ko: ClubDto,
    val en: ClubDto
)

data class ClubDto(
    val name: String,
    val description: String
)
