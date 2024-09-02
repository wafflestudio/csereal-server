package com.wafflestudio.csereal.core.about.api.req

data class UpdateAboutReq(
    val ko: BasicAbout,
    val en: BasicAbout,
    val removeImage: Boolean
)

data class BasicAbout(
    val description: String,
    val deleteIds: List<Long>
)
