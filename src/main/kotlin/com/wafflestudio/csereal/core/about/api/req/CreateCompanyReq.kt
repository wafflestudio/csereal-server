package com.wafflestudio.csereal.core.about.api.req

data class CreateCompanyReq(
    val name: String,
    val url: String?,
    val year: Int
)
