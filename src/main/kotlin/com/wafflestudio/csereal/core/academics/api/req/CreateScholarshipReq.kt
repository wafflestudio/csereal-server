package com.wafflestudio.csereal.core.academics.api.req

// 학부/대학원 구분은 경로에 있고, 이름·설명만 언어별이다.
data class CreateScholarshipReq(
    val ko: ScholarshipContentReq,
    val en: ScholarshipContentReq
)

data class ScholarshipContentReq(
    val name: String,
    val description: String
)
