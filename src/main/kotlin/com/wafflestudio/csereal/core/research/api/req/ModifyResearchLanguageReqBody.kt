package com.wafflestudio.csereal.core.research.api.req

import io.swagger.v3.oas.annotations.media.Schema
data class ModifyResearchLanguageReqBody(
    val websiteURL: String? = null,
    @Schema(description = "대표이미지를 뗀다. 새 이미지를 함께 보내면 교체가 우선이라 이 값은 무시된다.")
    val removeImage: Boolean,
    val ko: ResearchContentReqBody,
    val en: ResearchContentReqBody
)
