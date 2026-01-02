package com.wafflestudio.csereal.core.about.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.core.about.dto.FutureCareersStatDegreeDto
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity(name = "stat")
class StatEntity(
    var year: Int,

    @Enumerated(EnumType.STRING)
    var degree: Degree,
    var name: String,
    var count: Int
) : BaseTimeEntity() {
    companion object {
        fun of(year: Int, degree: Degree, statDto: FutureCareersStatDegreeDto): StatEntity {
            return StatEntity(
                year = year,
                degree = degree,
                name = statDto.name,
                count = statDto.count
            )
        }
    }
}

enum class Degree {
    BACHELOR, MASTER, DOCTOR
}
