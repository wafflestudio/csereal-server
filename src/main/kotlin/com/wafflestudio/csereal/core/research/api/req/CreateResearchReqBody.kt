package com.wafflestudio.csereal.core.research.api.req

import com.wafflestudio.csereal.core.research.type.ResearchType

data class CreateResearchReqBody(
    val postType: ResearchType,
    val name: String,
    val description: String?,
    val websiteURL: String?,
)
