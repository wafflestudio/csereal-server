package com.wafflestudio.csereal.core.member.api.req

data class ModifyProfessorLanguagesReqBody(
    val ko: ModifyProfessorReqBody,
    val en: ModifyProfessorReqBody,
)
