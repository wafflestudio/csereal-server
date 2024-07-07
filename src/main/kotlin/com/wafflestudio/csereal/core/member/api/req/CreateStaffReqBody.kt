package com.wafflestudio.csereal.core.member.api.req

data class CreateStaffReqBody(
    val language: String,
    val name: String,
    val role: String,
    val office: String,
    val phone: String,
    val email: String,
    val tasks: List<String>
)
