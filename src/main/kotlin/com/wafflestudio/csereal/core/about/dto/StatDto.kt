package com.wafflestudio.csereal.core.about.dto

data class StatDto(
    val year: Int,
    val bachelor: List<CompanyNameAndCountDto>,
    val master: List<CompanyNameAndCountDto>,
    val doctor: List<CompanyNameAndCountDto>
) {
}