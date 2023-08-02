package com.wafflestudio.csereal.core.news.dto

import com.querydsl.core.annotations.QueryProjection
import java.time.LocalDateTime

class NewsSearchResponse @QueryProjection constructor(
    val total: Int,
    val searchList: List<NewsSearchDto>
) {
}