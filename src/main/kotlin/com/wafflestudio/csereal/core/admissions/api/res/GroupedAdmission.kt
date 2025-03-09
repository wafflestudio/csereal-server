package com.wafflestudio.csereal.core.admissions.api.res

import com.wafflestudio.csereal.core.admissions.dto.AdmissionsDto

data class GroupedAdmission(
    val ko: AdmissionsDto,
    val en: AdmissionsDto
)
