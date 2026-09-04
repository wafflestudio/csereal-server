package com.wafflestudio.csereal.core.research.api.req

data class ModifyResearchLanguageReqBody(
    val websiteURL: String? = null,
    val removeImage: Boolean,
    val ko: ResearchContentReqBody,
    val en: ResearchContentReqBody
)
