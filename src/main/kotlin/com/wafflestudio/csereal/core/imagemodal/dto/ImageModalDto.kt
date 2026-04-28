package com.wafflestudio.csereal.core.imagemodal.dto

import com.wafflestudio.csereal.core.imagemodal.database.ImageModalEntity
import java.time.LocalDateTime

data class ImageModalDto(
    val id: Long,
    val titleKo: String?,
    val titleEn: String?,
    val imageAltKo: String?,
    val imageAltEn: String?,
    val displayUntil: LocalDateTime?,
    val externalLink: String?,

    val imageUrl: String
) {
    companion object {
        fun of(
            entity: ImageModalEntity,
            imageUrl: String
        ): ImageModalDto = entity.run {
            ImageModalDto(
                id = this.id,
                titleKo = this.titleKo,
                titleEn = this.titleEn,
                imageAltKo = this.imageAltKo,
                imageAltEn = this.imageAltEn,
                displayUntil = this.displayUntil,
                externalLink = this.externalLink,
                imageUrl = imageUrl
            )
        }
    }
}
