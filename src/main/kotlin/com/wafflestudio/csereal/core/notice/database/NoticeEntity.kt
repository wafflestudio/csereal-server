package com.wafflestudio.csereal.core.notice.database

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
    @Column(columnDefinition = "mediumtext")
    var description: String,

    @Column(columnDefinition = "mediumtext")
    var plainTextDescription: String,

    var isPublic: Boolean,
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
        this.title = updateNoticeRequest.title
        this.description = updateNoticeRequest.description
        this.isPublic = updateNoticeRequest.isPublic
        this.isPinned = updateNoticeRequest.isPinned
        this.isImportant = updateNoticeRequest.isImportant
    }

}
