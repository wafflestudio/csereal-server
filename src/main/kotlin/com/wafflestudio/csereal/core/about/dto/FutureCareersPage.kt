package com.wafflestudio.csereal.core.about.dto

data class FutureCareersPage(
    val description: String,
    val stat: List<StatDto>,
    val companies: List<CompanyDto>
) {
}