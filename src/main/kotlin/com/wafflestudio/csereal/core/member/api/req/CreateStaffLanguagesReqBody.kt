package com.wafflestudio.csereal.core.member.api.req

// 연락처는 언어와 무관하므로 최상위에 둔다 — 언어별 본문에 두면 한/영이 어긋날 수 있다.
// office 는 예외 — 호실 표기가 언어마다 달라 언어별 본문에 있다.
data class CreateStaffLanguagesReqBody(
    val phone: String,
    val email: String,
    val ko: CreateStaffReqBody,
    val en: CreateStaffReqBody
)

data class CreateStaffReqBody(
    val name: String,
    val role: String,
    val office: String,
    val tasks: List<String>
)
