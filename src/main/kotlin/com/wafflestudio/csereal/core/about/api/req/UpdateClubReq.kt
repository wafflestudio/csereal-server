package com.wafflestudio.csereal.core.about.api.req

import com.wafflestudio.csereal.core.about.dto.ClubDto

data class UpdateClubReq(
    val ko: ClubDto,
    val en: ClubDto,
    val removeImage: Boolean
)
