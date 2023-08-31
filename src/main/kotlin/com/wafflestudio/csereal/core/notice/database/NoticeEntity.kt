package com.wafflestudio.csereal.core.notice.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.notice.dto.NoticeDto
import com.wafflestudio.csereal.core.user.database.UserEntity
import jakarta.persistence.*


@Entity(name = "notice")
class NoticeEntity(

    var isDeleted: Boolean = false,

    var title: String,

    var description: String,

    var isPublic: Boolean,

    var isPinned: Boolean,

    @OneToMany(mappedBy = "notice", cascade = [CascadeType.ALL])
    var noticeTags: MutableSet<NoticeTagEntity> = mutableSetOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    val author: UserEntity
) : BaseTimeEntity() {
    fun update(updateNoticeRequest: NoticeDto) {
        this.title = updateNoticeRequest.title
        this.description = updateNoticeRequest.description
        this.isPublic = updateNoticeRequest.isPublic
        this.isPinned = updateNoticeRequest.isPinned
    }

}
