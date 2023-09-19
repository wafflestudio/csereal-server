package com.wafflestudio.csereal.core.about.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity(name = "location")
class LocationEntity(
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "about_id")
    val about: AboutEntity
) : BaseTimeEntity() {
    companion object {
        fun create(name: String, about: AboutEntity): LocationEntity {
            val locationEntity = LocationEntity(
                name = name,
                about = about
            )
            about.locations.add(locationEntity)
            return locationEntity
        }
    }
}
