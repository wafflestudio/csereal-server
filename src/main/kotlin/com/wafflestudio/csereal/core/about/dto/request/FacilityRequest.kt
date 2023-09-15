package com.wafflestudio.csereal.core.about.dto.request

data class FacilityRequest(
    val name: String,
    val description: String,
    val locations: List<String>,
) {
}