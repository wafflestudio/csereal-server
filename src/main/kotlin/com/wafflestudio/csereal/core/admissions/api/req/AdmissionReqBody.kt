package com.wafflestudio.csereal.core.admissions.api.req

import org.jetbrains.annotations.NotNull

data class AdmissionReqBody(
    @field:NotNull val name: String?,
    val language: String = "ko",
    @field:NotNull val description: String?
)
