package com.wafflestudio.csereal.core.notice.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity


@Entity(name = "post")
class NoticeEntity(
    @Column
    var title: String,

    @Column(columnDefinition = "text")
    var description: String
): BaseTimeEntity() {

}