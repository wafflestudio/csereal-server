package com.wafflestudio.csereal.core.notice.database

import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.entity.AttachmentAttachable
import com.wafflestudio.csereal.core.notice.api.req.NoticeReqBody
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.user.database.UserEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity(name = "notice")
class NoticeEntity(
    var title: String,

    @Column(columnDefinition = "text")
    var titleForMain: String?,

    @Column(columnDefinition = "mediumtext")
    var description: String,

    @Column(columnDefinition = "mediumtext")
    var plainTextDescription: String,

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

) : BaseTimeEntity(), AttachmentAttachable {

    companion object {
        fun of(req: NoticeReqBody, author: UserEntity) = NoticeEntity(
            title = req.title,
            titleForMain = req.titleForMain,
            description = req.description,
            plainTextDescription = cleanTextFromHtml(req.description),
            isPrivate = req.isPrivate,
            isPinned = req.isPinned,
            pinnedUntil = if (req.isPinned) req.pinnedUntil else null,
            isImportant = req.isImportant,
            importantUntil = if (req.isImportant) req.importantUntil else null,
            author = author
        )
    }

    fun update(updateNoticeRequest: NoticeReqBody) {
        // Update plainTextDescription if description is changed
        if (updateNoticeRequest.description != this.description) {
            this.plainTextDescription = cleanTextFromHtml(updateNoticeRequest.description)
        }

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
}
