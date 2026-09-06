package com.wafflestudio.csereal.core.member.api.req

import io.swagger.v3.oas.annotations.media.Schema
data class ModifyStaffLanguagesReqBody(
    val phone: String,
    val email: String,
    @Schema(description = "대표이미지를 뗀다. 새 이미지를 함께 보내면 교체가 우선이라 이 값은 무시된다.")
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
