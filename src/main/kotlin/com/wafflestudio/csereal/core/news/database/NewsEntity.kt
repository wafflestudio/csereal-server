package com.wafflestudio.csereal.core.news.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.common.controller.MainImageContentEntityType
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity(name = "news")
class NewsEntity(

    var isDeleted: Boolean = false,
    var title: String,

    @Column(columnDefinition = "mediumtext")
    var description: String,

    @Column(columnDefinition = "mediumtext")
    var plainTextDescription: String,

    var date: LocalDateTime?,
    var isPrivate: Boolean,
    var isSlide: Boolean,
    var isImportant: Boolean,

    @OneToOne
    var mainImage: MainImageEntity? = null,

    @OneToMany(mappedBy = "news", cascade = [CascadeType.ALL], orphanRemoval = true)
    var attachments: MutableList<AttachmentEntity> = mutableListOf(),

    @OneToMany(mappedBy = "news", cascade = [CascadeType.ALL])
    var newsTags: MutableSet<NewsTagEntity> = mutableSetOf()

) : BaseTimeEntity(), MainImageContentEntityType, AttachmentContentEntityType {
    override fun bringMainImage() = mainImage
    override fun bringAttachments() = attachments

    companion object {
        fun of(newsDto: NewsDto): NewsEntity {
            return NewsEntity(
                title = newsDto.title,
                description = newsDto.description,
                plainTextDescription = cleanTextFromHtml(newsDto.description),
                date = newsDto.date,
                isPrivate = newsDto.isPrivate,
                isSlide = newsDto.isSlide,
                isImportant = newsDto.isImportant,
            )
        }
    }

    fun update(updateNewsRequest: NewsDto) {
        if (updateNewsRequest.description != this.description) {
            this.description = updateNewsRequest.description
            this.plainTextDescription = cleanTextFromHtml(updateNewsRequest.description)
        }
        this.title = updateNewsRequest.title
        this.date = updateNewsRequest.date
        this.isPrivate = updateNewsRequest.isPrivate
        this.isSlide = updateNewsRequest.isSlide
        this.isImportant = updateNewsRequest.isImportant
    }
}
