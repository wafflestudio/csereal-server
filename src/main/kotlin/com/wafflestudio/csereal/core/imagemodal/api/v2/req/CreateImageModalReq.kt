package com.wafflestudio.csereal.core.imagemodal.api.v2.req

import java.time.LocalDateTime

data class CreateImageModalReq(
    val titleKo: String?,
    val titleEn: String?,
    val imageAltKo: String?,
    val imageAltEn: String?,
    val displayUntil: LocalDateTime?,
    val externalLink: String?
)
