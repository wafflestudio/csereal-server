package com.wafflestudio.csereal.core.about.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.common.controller.MainImageContentEntityType
import com.wafflestudio.csereal.core.about.dto.AboutDto
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
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

    @OneToMany(mappedBy = "")
    var attachments: MutableList<AttachmentEntity> = mutableListOf(),

    @OneToOne
    var mainImage: MainImageEntity? = null,

    ) : BaseTimeEntity(), MainImageContentEntityType, AttachmentContentEntityType {
    override fun bringMainImage(): MainImageEntity? = mainImage
    override fun bringAttachments(): List<AttachmentEntity> = attachments

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