package com.wafflestudio.csereal.core.research.api.req

data class ModifyLabLanguageReqBody(
    val groupId: Long?,
    val professorIds: Set<Long>,
    val acronym: String?,
    val tel: String?,
    val youtube: String?,
    val websiteURL: String?,
    val removePdf: Boolean,
    val ko: ModifyLabReqBody,
    val en: ModifyLabReqBody
)

data class ModifyLabReqBody(
    val name: String,
    val description: String?,
    val location: String?
)
