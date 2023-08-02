package com.wafflestudio.csereal.core.notice.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.notice.dto.NoticeDto
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany


@Entity(name = "notice")
class NoticeEntity(

    @Column
    var isDeleted: Boolean = false,

    @Column
    var title: String,

    @Column(columnDefinition = "text")
    var description: String,

//    var postType: String,

    var isPublic: Boolean,

    var isSlide: Boolean,

    var isPinned: Boolean,

    @OneToMany(mappedBy = "notice", cascade = [CascadeType.ALL])
    var noticeTags: MutableSet<NoticeTagEntity> = mutableSetOf()
): BaseTimeEntity() {

    fun update(updateNoticeRequest: NoticeDto) {
        this.title = updateNoticeRequest.title
        this.description = updateNoticeRequest.description
        this.isPublic = updateNoticeRequest.isPublic
        this.isSlide = updateNoticeRequest.isSlide
        this.isPinned = updateNoticeRequest.isPinned
    }

}