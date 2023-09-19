package com.wafflestudio.csereal.core.research.dto

import com.wafflestudio.csereal.core.research.database.LabEntity

data class LabDto(
    val id: Long,
    val name: String,
    val professors: List<LabProfessorResponse>?,
    val location: String?,
    val tel: String?,
    val acronym: String?,
    val pdf: String?,
    val youtube: String?,
    val group: String,
    val description: String?,
    val websiteURL: String?
) {
    companion object {
        fun of(entity: LabEntity, pdfURL: String): LabDto = entity.run {
            LabDto(
                id = this.id,
                name = this.name,
                professors = this.professors.map { LabProfessorResponse(id = it.id, name = it.name) },
                location = this.location,
                tel = this.tel,
                acronym = this.acronym,
                pdf = pdfURL,
                youtube = this.youtube,
                group = this.research.name,
                description = this.description,
                websiteURL = this.websiteURL
            )
        }
    }
}
