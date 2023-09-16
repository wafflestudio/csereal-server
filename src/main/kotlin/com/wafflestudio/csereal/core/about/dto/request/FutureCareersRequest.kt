package com.wafflestudio.csereal.core.about.dto.request

import com.wafflestudio.csereal.core.about.dto.FutureCareersCompanyDto
import com.wafflestudio.csereal.core.about.dto.FutureCareersStatDto

data class FutureCareersRequest(
    val description: String,
    val stat: List<FutureCareersStatDto>,
    val companies: List<FutureCareersCompanyDto>
) {
}