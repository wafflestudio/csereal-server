package com.wafflestudio.csereal.core.academics.api.req

data class UpdateYearReq(
    val description: String,
    val attachmentIds: List<Long>
)
