package com.wafflestudio.csereal.core.research.api.req

data class ModifyLabLanguageReqBody(
    val ko: ModifyLabReqBody,
    val en: ModifyLabReqBody
)

data class ModifyLabReqBody(
    val name: String,
    val description: String?,
    val location: String?,
    val tel: String?,
    val acronym: String?,
    val youtube: String?,
    val websiteURL: String?,
    val groupId: Long?,
    val professorIds: Set<Long>,
    val removePdf: Boolean
)
