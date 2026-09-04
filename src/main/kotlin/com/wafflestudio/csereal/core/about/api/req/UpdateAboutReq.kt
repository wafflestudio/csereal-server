package com.wafflestudio.csereal.core.about.api.req

// 첨부는 콘텐츠에 하나뿐이라 최상위에 둔다 — 예전엔 언어별로 따로 받아 같은 파일이 두 벌 올라갔다.
data class UpdateAboutReq(
    val removeImage: Boolean,
    val attachmentIds: List<Long>? = null,
    val ko: BasicAbout,
    val en: BasicAbout
)

data class BasicAbout(
    val description: String
)
