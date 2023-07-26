package com.wafflestudio.csereal.core.notice.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity


@Entity(name = "notice")
class NoticeEntity(
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
): BaseTimeEntity() {

}