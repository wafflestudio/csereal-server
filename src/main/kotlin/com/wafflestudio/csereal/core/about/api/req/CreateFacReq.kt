package com.wafflestudio.csereal.core.about.api.req

data class CreateFacReq(
    val ko: FacDto,
    val en: FacDto
)

data class FacDto(
    val name: String,
    val description: String,
    val locations: MutableList<String>
)
