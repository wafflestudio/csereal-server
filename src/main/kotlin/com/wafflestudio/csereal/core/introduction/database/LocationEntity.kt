package com.wafflestudio.csereal.core.introduction.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity(name = "location")
class LocationEntity(
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "introduction_id")
    val introduction: IntroductionEntity
) : BaseTimeEntity() {
    companion object {
        fun create(name: String, introduction: IntroductionEntity): LocationEntity {
            val locationEntity = LocationEntity(
                name = name,
                introduction = introduction
            )
            introduction.locations.add(locationEntity)
            return locationEntity
        }
    }
}