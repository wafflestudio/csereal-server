package com.wafflestudio.csereal.core.about.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity(name = "stat")
class StatEntity(
    var year: Int,

    @Enumerated(EnumType.STRING)
    var degree: Degree,
    var name: String,
    var count: Int,
): BaseTimeEntity() {
}

enum class Degree {
    BACHELOR, MASTER, DOCTOR
}