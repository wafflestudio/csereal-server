package com.wafflestudio.csereal.core.news.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.entity.AttachmentAttachable
import com.wafflestudio.csereal.common.entity.MainImageAttachable
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.LocalDate

@Entity(name = "news")
class NewsEntity(
    var title: String,

    @Column(columnDefinition = "text")
    var titleForMain: String?,

    @Column(columnDefinition = "mediumtext")
    var description: String,

    @Column(columnDefinition = "mediumtext")
    var plainTextDescription: String,

    var date: LocalDateTime,
    var isPrivate: Boolean,
    var isSlide: Boolean,
    var isImportant: Boolean,
    var importantUntil: LocalDate? = null,

    @OneToOne
    override var mainImage: MainImageEntity? = null,

    @OneToMany(mappedBy = "news", cascade = [CascadeType.ALL], orphanRemoval = true)
    override var attachments: MutableList<AttachmentEntity> = mutableListOf(),

    @OneToMany(mappedBy = "news", cascade = [CascadeType.ALL])
    var newsTags: MutableSet<NewsTagEntity> = mutableSetOf()

) : BaseTimeEntity(), MainImageAttachable, AttachmentAttachable {

    companion object {
        fun of(newsDto: NewsDto): NewsEntity {
            return NewsEntity(
                title = newsDto.title,
                titleForMain = newsDto.titleForMain,
                description = newsDto.description,
                plainTextDescription = cleanTextFromHtml(newsDto.description),
                date = newsDto.date,
                isPrivate = newsDto.isPrivate,
                isSlide = newsDto.isSlide,
                isImportant = newsDto.isImportant,
                importantUntil = if (newsDto.isImportant) newsDto.importantUntil else null
            )
        }
    }

    fun update(updateNewsRequest: NewsDto) {
        if (updateNewsRequest.description != this.description) {
            this.description = updateNewsRequest.description
            this.plainTextDescription = cleanTextFromHtml(updateNewsRequest.description)
        }
        this.title = updateNewsRequest.title
        this.titleForMain = updateNewsRequest.titleForMain
        this.date = updateNewsRequest.date
        this.isPrivate = updateNewsRequest.isPrivate
        this.isSlide = updateNewsRequest.isSlide
        this.isImportant = updateNewsRequest.isImportant
        this.importantUntil = if (updateNewsRequest.isImportant) updateNewsRequest.importantUntil else null
    }
}
