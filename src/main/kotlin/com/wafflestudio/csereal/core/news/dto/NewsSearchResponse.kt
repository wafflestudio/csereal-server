package com.wafflestudio.csereal.core.news.dto

import com.querydsl.core.annotations.QueryProjection

class NewsSearchResponse @QueryProjection constructor(
    val total: Long,
    val searchList: List<NewsSearchDto>
)
