package com.wafflestudio.csereal.core.notice.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity(name="noticeTag")
class NoticeTagEntity(
    @ManyToOne(cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "notice_id")
    var notice: NoticeEntity,

    @ManyToOne(cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "tag_id")
    var tag: TagEntity,

    ) : BaseTimeEntity() {
}