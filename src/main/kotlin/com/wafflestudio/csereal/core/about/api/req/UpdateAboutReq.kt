package com.wafflestudio.csereal.core.about.api.req

import io.swagger.v3.oas.annotations.media.Schema
// 첨부는 콘텐츠에 하나뿐이라 최상위에 둔다.
data class UpdateAboutReq(
    @Schema(description = "대표이미지를 뗀다. 새 이미지를 함께 보내면 교체가 우선이라 이 값은 무시된다.")
    val removeImage: Boolean,
    val attachmentIds: List<Long>? = null,
    val ko: BasicAbout,
    val en: BasicAbout
)

data class BasicAbout(
    val description: String
)
