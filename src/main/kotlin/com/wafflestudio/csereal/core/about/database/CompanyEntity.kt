package com.wafflestudio.csereal.core.about.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Entity

@Entity(name = "company")
class CompanyEntity(
    var name: String,
    var url: String,
    var year: Int,
) : BaseTimeEntity() {
}