package com.wafflestudio.csereal.core.research.api.req

// 소속·연락처·PDF 는 언어와 무관하므로 최상위에 둔다.
data class CreateLabLanguageReqBody(
    val groupId: Long?,
    val professorIds: Set<Long>,
    val acronym: String?,
    val tel: String?,
    val youtube: String?,
    val websiteURL: String?,
    val ko: CreateLabReqBody,
    val en: CreateLabReqBody
)

data class CreateLabReqBody(
    val name: String,
    val description: String?,
    val location: String?
)
