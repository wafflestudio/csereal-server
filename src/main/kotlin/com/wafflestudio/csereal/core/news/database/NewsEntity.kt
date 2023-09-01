package com.wafflestudio.csereal.core.news.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.common.controller.ImageContentEntityType
import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import jakarta.persistence.*

@Entity(name = "news")
class NewsEntity(

    var isDeleted: Boolean = false,

    var title: String,

    var description: String,

    var isPublic: Boolean,

    var isSlide: Boolean,

    @OneToOne
    var mainImage: MainImageEntity? = null,

    @OneToMany(mappedBy = "news", cascade = [CascadeType.ALL], orphanRemoval = true)
    var attachments: MutableList<AttachmentEntity> = mutableListOf(),

    @OneToMany(mappedBy = "news", cascade = [CascadeType.ALL])
    var newsTags: MutableSet<NewsTagEntity> = mutableSetOf()

): BaseTimeEntity(), ImageContentEntityType, AttachmentContentEntityType {
    override fun bringMainImage() = mainImage
    override fun bringAttachments() = attachments

    companion object {
        fun of(newsDto: NewsDto): NewsEntity {
            return NewsEntity(
                title = newsDto.title,
                description = newsDto.description,
                isPublic = newsDto.isPublic,
                isSlide = newsDto.isSlide,
            )
        }
    }
    fun update(updateNewsRequest: NewsDto) {
        this.title = updateNewsRequest.title
        this.description = updateNewsRequest.description
        this.isPublic = updateNewsRequest.isPublic
        this.isSlide = updateNewsRequest.isSlide
    }
}