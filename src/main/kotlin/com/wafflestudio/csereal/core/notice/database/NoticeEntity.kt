package com.wafflestudio.csereal.core.notice.database

import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.entity.AttachmentAttachable
import com.wafflestudio.csereal.core.notice.api.req.NoticeReqBody
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.common.sanitize.HtmlContentHolder
import com.wafflestudio.csereal.common.sanitize.HtmlField
import com.wafflestudio.csereal.common.search.SearchIndexed
import com.wafflestudio.csereal.common.search.SearchType
import jakarta.persistence.*
import java.time.LocalDate

@Entity(name = "notice")
class NoticeEntity(
    var title: String,

    @Column(columnDefinition = "text")
    var titleForMain: String?,

    description: String,

    var isPrivate: Boolean,

    var isPinned: Boolean,
    var pinnedUntil: LocalDate? = null,

    var isImportant: Boolean,
    var importantUntil: LocalDate? = null,

    @OneToMany(mappedBy = "notice", cascade = [CascadeType.ALL])
    var noticeTags: MutableSet<NoticeTagEntity> = mutableSetOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    val author: UserEntity,

    @OneToMany(mappedBy = "notice", cascade = [CascadeType.ALL], orphanRemoval = true)
    override var attachments: MutableList<AttachmentEntity> = mutableListOf()

) : BaseTimeEntity(), AttachmentAttachable, SearchIndexed, HtmlContentHolder {

    override val searchType get() = SearchType.NOTICE
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
        fun of(req: NoticeReqBody, author: UserEntity) = NoticeEntity(
            title = req.title,
            titleForMain = req.titleForMain,
            description = req.description,
            isPrivate = req.isPrivate,
            isPinned = req.isPinned,
            pinnedUntil = if (req.isPinned) req.pinnedUntil else null,
            isImportant = req.isImportant,
            importantUntil = if (req.isImportant) req.importantUntil else null,
            author = author
        )
    }

    fun update(updateNoticeRequest: NoticeReqBody) {
        this.title = updateNoticeRequest.title
        this.titleForMain = updateNoticeRequest.titleForMain
        this.description = updateNoticeRequest.description
        this.isPrivate = updateNoticeRequest.isPrivate

        // Pin related fields (prioritize isPinned flag)
        this.isPinned = updateNoticeRequest.isPinned
        this.pinnedUntil = if (updateNoticeRequest.isPinned) updateNoticeRequest.pinnedUntil else null

        // Important related fields (prioritize isImportant flag)
        this.isImportant = updateNoticeRequest.isImportant
        this.importantUntil = if (updateNoticeRequest.isImportant) updateNoticeRequest.importantUntil else null
    }

    override fun attach(attachment: AttachmentEntity) {
        attachments.add(attachment)
        attachment.notice = this
    }
}
