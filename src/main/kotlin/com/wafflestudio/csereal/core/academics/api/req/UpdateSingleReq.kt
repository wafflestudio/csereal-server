package com.wafflestudio.csereal.core.academics.api.req

data class UpdateSingleReq(
    val description: String,
    val deleteIds: List<Long>
)
