package com.wafflestudio.csereal.core.about.dto

import com.wafflestudio.csereal.core.about.database.StatEntity

data class FutureCareersStatDegreeDto(
    val id: Long,
    val name: String,
    val count: Int
) {
    companion object {
        fun of(entity: StatEntity): FutureCareersStatDegreeDto = entity.run {
            FutureCareersStatDegreeDto(
                id = this.id,
                name = this.name,
                count = this.count
            )
        }
    }
}
