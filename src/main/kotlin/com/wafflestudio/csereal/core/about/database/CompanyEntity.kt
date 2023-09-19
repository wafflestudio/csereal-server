package com.wafflestudio.csereal.core.about.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.about.dto.FutureCareersCompanyDto
import jakarta.persistence.Entity

@Entity(name = "company")
class CompanyEntity(
    var name: String,
    var url: String?,
    var year: Int?
) : BaseTimeEntity() {
    companion object {
        fun of(companyDto: FutureCareersCompanyDto): CompanyEntity {
            return CompanyEntity(
                name = companyDto.name,
                url = companyDto.url,
                year = companyDto.year
            )
        }
    }
}
