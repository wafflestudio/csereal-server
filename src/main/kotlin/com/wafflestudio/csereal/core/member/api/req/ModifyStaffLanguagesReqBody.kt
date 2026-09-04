package com.wafflestudio.csereal.core.member.api.req

data class ModifyStaffLanguagesReqBody(
    val phone: String,
    val email: String,
    val removeImage: Boolean,
    val ko: ModifyStaffReqBody,
    val en: ModifyStaffReqBody
)

data class ModifyStaffReqBody(
    val name: String,
    val role: String,
    val office: String,
    val tasks: List<String>
)
