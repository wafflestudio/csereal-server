package com.wafflestudio.csereal.core.resource.attachment.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.about.database.AboutEntity
import com.wafflestudio.csereal.core.academics.database.AcademicsEntity
import com.wafflestudio.csereal.core.academics.database.CourseEntity
import com.wafflestudio.csereal.core.academics.database.ScholarshipEntity
import com.wafflestudio.csereal.core.council.database.CouncilFileEntity
import com.wafflestudio.csereal.core.news.database.NewsEntity
import com.wafflestudio.csereal.core.notice.database.NoticeEntity
import com.wafflestudio.csereal.core.research.database.LabEntity
import com.wafflestudio.csereal.core.seminar.database.SeminarEntity
import jakarta.persistence.*

@Entity(name = "attachment")
class AttachmentEntity(
    var isDeleted: Boolean? = false,

    @Column(unique = true)
    val filename: String,

    val attachmentsOrder: Int,
    val size: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id")
    var news: NewsEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    var notice: NoticeEntity? = null,

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scholarship_id")
    var scholarship: ScholarshipEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "council_file_id")
    var councilFile: CouncilFileEntity? = null
) : BaseTimeEntity()
