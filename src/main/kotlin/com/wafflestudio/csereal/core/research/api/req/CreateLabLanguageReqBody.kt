package com.wafflestudio.csereal.core.research.api.req

import com.wafflestudio.csereal.core.research.dto.LabProfessorResponse
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse

data class CreateLabLanguageReqBody(
    val ko: CreateLabReqBody,
    val en: CreateLabReqBody,
)

data class CreateLabReqBody(
    val name: String,
    val description: String?,
    val groupId: Long?,
    val professorIds: Set<Long>,
    val location: String?,
    val tel: String?,
    val acronym: String?,
    val youtube: String?,
    val websiteURL: String?
)
