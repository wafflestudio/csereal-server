package com.wafflestudio.csereal.core.research.dto

import com.wafflestudio.csereal.core.research.database.LabEntity

data class LabDto(
    val id: Long,
    val name: String,
    val initial: String?,
    val researchGroupId: Long,
    val professorsId: List<Long>?,
    val labs: String?,
    val phone: String?,
    val fax: String?,
    val website: String?,
    val description: String?,
) {
    companion object {
        fun of(researchGroupId: Long, entity: LabEntity): LabDto = entity.run {
            LabDto(
                id = this.id,
                name = this.name,
                initial = this.initial,
                researchGroupId = researchGroupId,
                professorsId = this.professors.map { it.id },
                labs = this.labs,
                phone = this.phone,
                fax = this.fax,
                website = this.website,
                description = this.description
            )
        }
    }
}