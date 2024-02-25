package com.wafflestudio.csereal.core.about.dto

data class FutureCareersRequest(
    val language: String,
    val description: String,
    val stat: List<FutureCareersStatDto>,
    val companies: List<FutureCareersCompanyDto>
)
