package com.wafflestudio.csereal.core.resource.attachment.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.about.database.AboutEntity
import com.wafflestudio.csereal.core.academics.database.AcademicsEntity
import com.wafflestudio.csereal.core.academics.database.CourseEntity
import com.wafflestudio.csereal.core.news.database.NewsEntity
import com.wafflestudio.csereal.core.research.database.LabEntity
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "about_id")
    var about: AboutEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academics_id")
    var academics: AcademicsEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    var course: CourseEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id")
    var lab: LabEntity? = null,
) : BaseTimeEntity() {

}