package com.wafflestudio.csereal.core.notice.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.OneToMany

@Entity(name = "tag_in_notice")
class TagInNoticeEntity(
    @Enumerated(EnumType.STRING)
    var name: TagInNoticeEnum,

    @OneToMany(mappedBy = "tag")
    val noticeTags: MutableSet<NoticeTagEntity> = mutableSetOf()
) : BaseTimeEntity()
