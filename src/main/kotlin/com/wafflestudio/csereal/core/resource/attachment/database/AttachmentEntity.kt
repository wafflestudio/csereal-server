package com.wafflestudio.csereal.core.resource.attachment.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.news.database.NewsEntity
import com.wafflestudio.csereal.core.notice.database.TagInNoticeEntity
import com.wafflestudio.csereal.core.seminar.database.SeminarEntity
import jakarta.persistence.*

@Entity(name = "attachment")
class AttachmentEntity(
    val isDeleted : Boolean? = false,

    @Column(unique = true)
    val filename: String,

    val attachmentsOrder: Int,
    val size: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id")
    var news: NewsEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seminar_id")
    var seminar: SeminarEntity? = null,

    ) : BaseTimeEntity() {

}