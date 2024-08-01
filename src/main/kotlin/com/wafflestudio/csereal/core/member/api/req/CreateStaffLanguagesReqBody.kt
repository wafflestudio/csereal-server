package com.wafflestudio.csereal.core.member.api.req

data class CreateStaffLanguagesReqBody(
    val ko: CreateStaffReqBody,
    val en: CreateStaffReqBody
)
