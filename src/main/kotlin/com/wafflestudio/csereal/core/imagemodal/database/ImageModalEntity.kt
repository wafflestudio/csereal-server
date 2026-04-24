package com.wafflestudio.csereal.core.imagemodal.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.entity.MainImageAttachable
import com.wafflestudio.csereal.core.imagemodal.api.req.CreateImageModalReq
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import jakarta.persistence.Entity
import jakarta.persistence.OneToOne
import java.time.LocalDateTime

@Entity(name = "image_modal")
class ImageModalEntity(
    var titleKo: String?,
    var titleEn: String?,
    var imageAltKo: String?,
    var imageAltEn: String?,

    var displayUntil: LocalDateTime? = null,

    var externalLink: String?,

    @OneToOne
    override var mainImage: MainImageEntity? = null
) : BaseTimeEntity(), MainImageAttachable {
    companion object {
        fun of(req: CreateImageModalReq): ImageModalEntity =
            ImageModalEntity(
                titleKo = req.titleKo,
                titleEn = req.titleEn,
                imageAltKo = req.imageAltKo,
                imageAltEn = req.imageAltEn,
                displayUntil = req.displayUntil,
                externalLink = req.externalLink
            )
    }

    fun update(req: CreateImageModalReq) {
        this.titleKo = req.titleKo
        this.titleEn = req.titleEn
        this.imageAltKo = req.imageAltKo
        this.imageAltEn = req.imageAltEn
        this.displayUntil = req.displayUntil
        this.externalLink = req.externalLink
    }
}
