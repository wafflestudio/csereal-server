package com.wafflestudio.csereal.core.member.api.req

data class CreateProfessorLanguagesReqBody(
    val ko: CreateProfessorReqBody,
    val en: CreateProfessorReqBody,
)

