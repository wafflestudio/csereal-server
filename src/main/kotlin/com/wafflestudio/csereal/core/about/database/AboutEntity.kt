package com.wafflestudio.csereal.core.about.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.ContentEntityType
import com.wafflestudio.csereal.core.about.dto.AboutDto
import com.wafflestudio.csereal.core.resource.image.database.ImageEntity
import jakarta.persistence.*

@Entity(name = "about")
class AboutEntity(
    @Enumerated(EnumType.STRING)
    var postType: AboutPostType,
    var name: String?,
    var engName: String?,
    var description: String,
    var year: Int?,

    @OneToMany(mappedBy = "about", cascade = [CascadeType.ALL], orphanRemoval = true)
    val locations: MutableList<LocationEntity> = mutableListOf(),

    @OneToOne(mappedBy = "about", cascade = [CascadeType.ALL])
    var mainImage: ImageEntity? = null,

) : BaseTimeEntity(), ContentEntityType {
    override fun bringMainImage(): ImageEntity? = mainImage

    companion object {
        fun of(postType: AboutPostType, aboutDto: AboutDto): AboutEntity {
            return AboutEntity(
                postType = postType,
                name = aboutDto.name,
                engName = aboutDto.engName,
                description = aboutDto.description,
                year = aboutDto.year,
            )
        }
    }
}