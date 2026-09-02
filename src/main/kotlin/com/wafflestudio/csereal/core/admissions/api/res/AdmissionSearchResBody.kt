package com.wafflestudio.csereal.core.admissions.api.res

data class AdmissionSearchResBody(
    val total: Long,
    val results: List<AdmissionSearchResElem>
)
