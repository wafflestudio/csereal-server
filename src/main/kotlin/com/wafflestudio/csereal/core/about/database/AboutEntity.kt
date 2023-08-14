package com.wafflestudio.csereal.core.about.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.about.dto.AboutDto
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity(name = "about")
class AboutEntity(
    var postType: String,

    var name: String,

    var engName: String?,

    var description: String,

    var year: Int?,

    var isPublic: Boolean,

    @OneToMany(mappedBy = "about", cascade = [CascadeType.ALL], orphanRemoval = true)
    val locations: MutableList<LocationEntity> = mutableListOf()
) : BaseTimeEntity() {
    companion object {
        fun of(aboutDto: AboutDto): AboutEntity {
            return AboutEntity(
                postType = aboutDto.postType,
                name = aboutDto.name,
                engName = aboutDto.engName,
                description = aboutDto.description,
                year = aboutDto.year,
                isPublic = aboutDto.isPublic,
            )
        }
    }
}