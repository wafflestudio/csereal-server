package com.wafflestudio.csereal.core.research.api.req

import com.wafflestudio.csereal.core.research.type.ResearchType

// type·websiteURL 은 언어와 무관하므로 최상위에 둔다.
// (예전엔 언어별 본문마다 type 이 있어 한/영이 다른 종류가 될 수 있었고, Jackson 다형성까지 필요했다.)
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
