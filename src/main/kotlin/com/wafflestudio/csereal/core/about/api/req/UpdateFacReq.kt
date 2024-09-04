package com.wafflestudio.csereal.core.about.api.req

data class UpdateFacReq(
    val ko: FacDto,
    val en: FacDto,
    val removeImage: Boolean
)
