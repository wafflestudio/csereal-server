package com.wafflestudio.csereal.core.member.api.req

import com.wafflestudio.csereal.core.member.database.ProfessorStatus
import java.time.LocalDate

// 신분·소속·연락처는 언어와 무관하므로 최상위에 둔다.
// office 는 예외 — 호실 표기가 언어마다 달라 언어별 본문에 있다.
data class CreateProfessorLanguagesReqBody(
    val status: ProfessorStatus,
    val labId: Long?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val phone: String?,
    val fax: String?,
    val email: String?,
    val website: String?,
    val ko: CreateProfessorReqBody,
    val en: CreateProfessorReqBody
)

data class CreateProfessorReqBody(
    val name: String,
    val academicRank: String,
    val department: String,
    val office: String?,
    val educations: List<String>,
    val researchAreas: List<String>,
    val careers: List<String>
)
