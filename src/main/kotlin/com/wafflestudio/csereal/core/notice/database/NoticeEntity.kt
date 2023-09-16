package com.wafflestudio.csereal.core.notice.database

import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.core.notice.dto.NoticeDto
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.user.database.UserEntity
import jakarta.persistence.*


@Entity(name = "notice")
class NoticeEntity(
    var isDeleted: Boolean = false,
    var title: String,
    var titleForMain: String?,

    @Column(columnDefinition = "mediumtext")
    var description: String,

    @Column(columnDefinition = "mediumtext")
    var plainTextDescription: String,

    var isPrivate: Boolean,
    var isPinned: Boolean,
    var isImportant: Boolean,

    @OneToMany(mappedBy = "notice", cascade = [CascadeType.ALL])
    var noticeTags: MutableSet<NoticeTagEntity> = mutableSetOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    val author: UserEntity,

    @OneToMany(mappedBy = "notice", cascade = [CascadeType.ALL], orphanRemoval = true)
    var attachments: MutableList<AttachmentEntity> = mutableListOf(),

    ) : BaseTimeEntity(), AttachmentContentEntityType {
    override fun bringAttachments() = attachments

    fun update(updateNoticeRequest: NoticeDto) {
        // Update plainTextDescription if description is changed
        if (updateNoticeRequest.description != this.description) {
            this.plainTextDescription = cleanTextFromHtml(updateNoticeRequest.description)
        }

        this.title = updateNoticeRequest.title
        this.titleForMain = updateNoticeRequest.titleForMain
        this.description = updateNoticeRequest.description
        this.isPrivate = updateNoticeRequest.isPrivate
        this.isPinned = updateNoticeRequest.isPinned
        this.isImportant = updateNoticeRequest.isImportant
    }

}
