package com.wafflestudio.csereal.core.about.dto

import com.wafflestudio.csereal.core.about.database.CompanyEntity

data class FutureCareersCompanyDto(
    val id: Long,
    val name: String,
    val url: String?,
    val year: Int?
) {
    companion object {
        fun of(entity: CompanyEntity): FutureCareersCompanyDto = entity.run {
            FutureCareersCompanyDto(
                id = this.id,
                name = this.name,
                url = this.url,
                year = this.year
            )
        }
    }
}