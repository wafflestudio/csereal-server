package com.wafflestudio.csereal.core.main.dto

import com.wafflestudio.csereal.core.about.api.res.AboutSearchResBody
import com.wafflestudio.csereal.core.academics.api.res.AcademicsSearchResBody
import com.wafflestudio.csereal.core.admissions.api.res.AdmissionSearchResBody
import com.wafflestudio.csereal.core.member.api.res.MemberSearchResBody
import com.wafflestudio.csereal.core.news.dto.NewsTotalSearchDto
import com.wafflestudio.csereal.core.notice.dto.NoticeTotalSearchResponse
import com.wafflestudio.csereal.core.research.api.res.ResearchSearchResBody
import com.wafflestudio.csereal.core.seminar.dto.SeminarSearchResponse

/**
 * v3 통합 검색 응답. TotalSearchResponse와 달리 모든 섹션이 nullable이다 —
 * 클라이언트가 `sections`로 원하는 섹션만 요청하면 나머지는 null.
 *
 * 이전엔 프론트가 8개 도메인 엔드포인트로 팬아웃해 직접 집계했다. 이제 백엔드가
 * 한 번에 집계해 돌려주므로 프론트는 1콜로 끝난다(섹션 트리/카운트도 이 응답에서 파생).
 */
data class SearchV3Response(
    val about: AboutSearchResBody? = null,
    val notice: NoticeTotalSearchResponse? = null,
    val news: NewsTotalSearchDto? = null,
    val seminar: SeminarSearchResponse? = null,
    val member: MemberSearchResBody? = null,
    val research: ResearchSearchResBody? = null,
    val admissions: AdmissionSearchResBody? = null,
    val academics: AcademicsSearchResBody? = null
)
