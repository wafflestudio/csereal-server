package com.wafflestudio.csereal.core.notice.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity(name="noticeTag")
class NoticeTagEntity(
    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "notice_id")
    val notice: NoticeEntity,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "tag_id")
    val tag: TagEntity,

    ) : BaseTimeEntity() {
}