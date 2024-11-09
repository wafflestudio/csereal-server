package com.wafflestudio.csereal.core.recruit.api.req

data class ModifyRecruitReqBody(
    val title: String,
    val description: String,
    val removeImage: Boolean
)
