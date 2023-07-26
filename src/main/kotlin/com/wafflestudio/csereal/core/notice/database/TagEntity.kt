package com.wafflestudio.csereal.core.notice.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity(name = "tag")
class TagEntity(
    var name: String,

    @OneToMany(mappedBy = "tag")
    val noticeTag: MutableSet<NoticeTagEntity> = mutableSetOf()
) : BaseTimeEntity() {
}