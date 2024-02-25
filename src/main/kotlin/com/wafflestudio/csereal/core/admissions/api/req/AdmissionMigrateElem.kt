package com.wafflestudio.csereal.core.admissions.api.req

import org.jetbrains.annotations.NotNull

data class AdmissionMigrateElem(
    @field:NotNull val name: String?,
    val mainType: String,
    val postType: String,
    val language: String,
    @field:NotNull val description: String?
)
