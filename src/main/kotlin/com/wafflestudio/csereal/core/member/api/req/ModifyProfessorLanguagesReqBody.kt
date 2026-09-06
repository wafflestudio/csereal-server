package com.wafflestudio.csereal.core.member.api.req

import io.swagger.v3.oas.annotations.media.Schema
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
    @Schema(description = "대표이미지를 뗀다. 새 이미지를 함께 보내면 교체가 우선이라 이 값은 무시된다.")
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
