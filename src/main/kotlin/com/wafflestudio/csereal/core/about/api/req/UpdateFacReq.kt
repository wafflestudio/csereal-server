package com.wafflestudio.csereal.core.about.api.req

import com.wafflestudio.csereal.core.about.dto.FacDto

data class UpdateFacReq(
    val ko: FacDto,
    val en: FacDto,
    val removeImage: Boolean
)
