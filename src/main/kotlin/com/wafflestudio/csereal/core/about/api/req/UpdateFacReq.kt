package com.wafflestudio.csereal.core.about.api.req

import com.wafflestudio.csereal.core.about.dto.FacReq

data class UpdateFacReq(
    val ko: FacReq,
    val en: FacReq,
    val removeImage: Boolean
)
