package com.wafflestudio.csereal.core.notice.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
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
//
//    var isPublic: Boolean,
//
//    var isSlide: Boolean,
//
//    var isPinned: Boolean,

    @OneToMany(mappedBy = "notice", cascade = [CascadeType.PERSIST])
    var noticeTag: MutableSet<NoticeTagEntity> = mutableSetOf()
): BaseTimeEntity() {

}