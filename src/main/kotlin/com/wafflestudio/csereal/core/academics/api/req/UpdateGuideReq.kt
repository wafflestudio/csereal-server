package com.wafflestudio.csereal.core.academics.api.req

data class UpdateGuideReq(
    val description: String,
    val deleteIds: List<Long>
)
