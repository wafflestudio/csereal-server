package com.wafflestudio.csereal.core.main.dto

import com.wafflestudio.csereal.core.about.api.res.AboutSearchResBody
import com.wafflestudio.csereal.core.academics.api.res.AcademicsSearchResBody
import com.wafflestudio.csereal.core.admissions.api.res.AdmissionSearchResBody
import com.wafflestudio.csereal.core.member.api.res.MemberSearchResBody
import com.wafflestudio.csereal.core.news.dto.NewsTotalSearchDto
import com.wafflestudio.csereal.core.notice.dto.NoticeTotalSearchResponse
import com.wafflestudio.csereal.core.research.api.res.ResearchSearchResBody
import com.wafflestudio.csereal.core.seminar.dto.SeminarSearchResponse

data class TotalSearchResponse(
    val aboutResult: AboutSearchResBody,
    val noticeResult: NoticeTotalSearchResponse,
    val newsResult: NewsTotalSearchDto,
    val seminarResult: SeminarSearchResponse,
    val memberResult: MemberSearchResBody,
    val researchResult: ResearchSearchResBody,
    val admissionsResult: AdmissionSearchResBody,
    val academicsResult: AcademicsSearchResBody
)
