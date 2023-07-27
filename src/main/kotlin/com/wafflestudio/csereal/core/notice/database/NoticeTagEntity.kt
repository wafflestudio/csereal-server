package com.wafflestudio.csereal.core.notice.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.*

@Entity(name = "noticeTag")
class NoticeTagEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    var notice: NoticeEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    var tag: TagEntity,

    ) : BaseTimeEntity() {
    companion object {

        fun createNoticeTag(notice: NoticeEntity, tag: TagEntity) {
            NoticeTagEntity(notice, tag)
            notice.noticeTags.add(NoticeTagEntity(notice, tag))
            tag.noticeTags.add(NoticeTagEntity(notice,tag))
        }
    }



}

