package com.wafflestudio.csereal.core.scholarship.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity(name = "scholarship")
class ScholarshipEntity(

    val title: String,

    @Column(columnDefinition = "text")
    val description: String

) : BaseTimeEntity() {

}
