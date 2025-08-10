package com.wafflestudio.csereal.core.council.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.domain.MainImageAttachable
import com.wafflestudio.csereal.core.council.dto.ReportCreateRequest
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import jakarta.persistence.*

@Entity(name = "council")
class CouncilEntity(
    @Enumerated(EnumType.STRING)
    val type: CouncilType,

    var title: String,

    @Column(columnDefinition = "mediumtext")
    var description: String,

    @OneToOne
    override var mainImage: MainImageEntity? = null,

    var sequence: Int,
    var name: String
) : BaseTimeEntity(), MainImageAttachable {

    companion object {
        fun createReport(req: ReportCreateRequest): CouncilEntity =
            CouncilEntity(
                type = CouncilType.REPORT,
                title = req.title,
                description = req.description,
                sequence = req.sequence,
                name = req.name
            )
    }
}

enum class CouncilType {
    INTRO, REPORT
}
