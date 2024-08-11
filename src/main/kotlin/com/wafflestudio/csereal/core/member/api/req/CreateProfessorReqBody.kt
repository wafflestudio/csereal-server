package com.wafflestudio.csereal.core.member.api.req

import com.wafflestudio.csereal.core.member.database.ProfessorStatus
import java.time.LocalDate

data class CreateProfessorReqBody(
    val name: String,
    val status: ProfessorStatus,
    val academicRank: String,
    val labId: Long?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val office: String?,
    val phone: String?,
    val fax: String?,
    val email: String?,
    val website: String?,
    val educations: List<String>,
    val researchAreas: List<String>,
    val careers: List<String>
)
