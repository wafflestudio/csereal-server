package com.wafflestudio.csereal.core.council.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.MainImageContentEntityType
import com.wafflestudio.csereal.core.council.dto.ReportCreateRequest
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import com.wafflestudio.csereal.core.user.database.UserEntity
import jakarta.persistence.*

@Entity(name = "council")
class CouncilEntity(

    var title: String,

    @Column(columnDefinition = "mediumtext")
    var description: String,

    @OneToOne
    var mainImage: MainImageEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    val author: UserEntity

) : BaseTimeEntity(), MainImageContentEntityType {
    override fun bringMainImage() = mainImage

    companion object {
        fun createReport(req: ReportCreateRequest, author: UserEntity): CouncilEntity =
            CouncilEntity(
                title = req.title,
                description = req.description,
                author = author
            )
    }
}
