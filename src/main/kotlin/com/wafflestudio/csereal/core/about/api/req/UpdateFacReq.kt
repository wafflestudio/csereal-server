package com.wafflestudio.csereal.core.about.api.req

import io.swagger.v3.oas.annotations.media.Schema
import com.wafflestudio.csereal.core.about.dto.FacReq

data class UpdateFacReq(
    val ko: FacReq,
    val en: FacReq,
    @Schema(description = "대표이미지를 뗀다. 새 이미지를 함께 보내면 교체가 우선이라 이 값은 무시된다.")
    val removeImage: Boolean
)
