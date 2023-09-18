package com.wafflestudio.csereal.core.about.dto

data class FutureCareersStatDto(
    val year: Int,
    val bachelor: List<FutureCareersStatDegreeDto>,
    val master: List<FutureCareersStatDegreeDto>,
    val doctor: List<FutureCareersStatDegreeDto>
) {
}