package com.wafflestudio.csereal.core.news.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.entity.AttachmentAttachable
import com.wafflestudio.csereal.common.entity.MainImageAttachable
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import com.wafflestudio.csereal.core.news.api.req.NewsReqBody
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import com.wafflestudio.csereal.common.sanitize.HtmlContentHolder
import com.wafflestudio.csereal.common.sanitize.HtmlField
import com.wafflestudio.csereal.common.search.SearchIndexed
import com.wafflestudio.csereal.common.search.SearchType
import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.LocalDate

@Entity(name = "news")
class NewsEntity(
    var title: String,

    @Column(columnDefinition = "text")
    var titleForMain: String?,

    description: String,

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

) : BaseTimeEntity(), MainImageAttachable, AttachmentAttachable, SearchIndexed, HtmlContentHolder {

    override val searchType get() = SearchType.NEWS
    override val searchSourceId get() = id

    /**
     * 본문. 목록·메인에 쓰이는 [plainTextDescription] 이 여기서 파생되므로
     * **대입할 때마다** 함께 갱신한다 — 누가 언제 바꾸든 둘이 어긋날 수 없다.
     * (Hibernate 는 필드 접근이라 DB 에서 읽을 땐 이 setter 를 타지 않는다.)
     */
    @Column(columnDefinition = "mediumtext")
    var description: String = description
        set(value) {
            field = value
            plainTextDescription = cleanTextFromHtml(value)
        }

    /** [description] 에서 태그를 걷어낸 것. 목록 미리보기와 검색 색인이 읽는다. */
    @Column(columnDefinition = "mediumtext")
    var plainTextDescription: String = cleanTextFromHtml(description)

    override fun htmlFields() = listOf(HtmlField({ description }, { description = it }))

    companion object {
        fun of(newsDto: NewsReqBody): NewsEntity {
            return NewsEntity(
                title = newsDto.title,
                titleForMain = newsDto.titleForMain,
                description = newsDto.description,
                date = newsDto.date,
                isPrivate = newsDto.isPrivate,
                isSlide = newsDto.isSlide,
                isImportant = newsDto.isImportant,
                importantUntil = if (newsDto.isImportant) newsDto.importantUntil else null
            )
        }
    }

    fun update(updateNewsRequest: NewsReqBody) {
        this.description = updateNewsRequest.description
        this.title = updateNewsRequest.title
        this.titleForMain = updateNewsRequest.titleForMain
        this.date = updateNewsRequest.date
        this.isPrivate = updateNewsRequest.isPrivate
        this.isSlide = updateNewsRequest.isSlide
        this.isImportant = updateNewsRequest.isImportant
        this.importantUntil = if (updateNewsRequest.isImportant) updateNewsRequest.importantUntil else null
    }

    override fun attach(attachment: AttachmentEntity) {
        attachments.add(attachment)
        attachment.news = this
    }
}
