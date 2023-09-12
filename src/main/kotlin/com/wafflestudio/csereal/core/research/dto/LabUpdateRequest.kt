package com.wafflestudio.csereal.core.research.dto

data class LabUpdateRequest(
        val name: String,
        val professorIds: List<Long>,
        val location: String?,
        val tel: String?,
        val acronym: String?,
        val youtube: String?,
        val description: String?,
        val websiteURL: String?,
        val pdfModified: Boolean,
)
