package com.wafflestudio.csereal.core.member.api.req

import com.wafflestudio.csereal.core.member.database.ProfessorStatus
import java.time.LocalDate

data class ModifyProfessorLanguagesReqBody(
    val status: ProfessorStatus,
    val labId: Long?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val phone: String?,
    val fax: String?,
    val email: String?,
    val website: String?,
    val removeImage: Boolean,
    val ko: ModifyProfessorReqBody,
    val en: ModifyProfessorReqBody
)

data class ModifyProfessorReqBody(
    val name: String,
    val academicRank: String,
    val department: String,
    val office: String?,
    val educations: List<String>,
    val researchAreas: List<String>,
    val careers: List<String>
)
