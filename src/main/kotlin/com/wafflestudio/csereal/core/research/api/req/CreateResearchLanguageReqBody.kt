package com.wafflestudio.csereal.core.research.api.req

import com.wafflestudio.csereal.core.research.type.ResearchType

// type·websiteURL 은 언어와 무관하므로 최상위에 둔다.
data class CreateResearchLanguageReqBody(
    val type: ResearchType,
    val websiteURL: String? = null,
    val ko: ResearchContentReqBody,
    val en: ResearchContentReqBody
)

data class ResearchContentReqBody(
    val name: String,
    val description: String
)
