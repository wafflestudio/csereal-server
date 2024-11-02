package com.wafflestudio.csereal.core.recruit.api.req

data class ModifyRecruitReqBody(
    val latestRecruitTitle: String,
    val latestRecruitUrl: String,
    val description: String,
    val removeImage: Boolean
)
