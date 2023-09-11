package com.wafflestudio.csereal.core.main.dto

import com.querydsl.core.annotations.QueryProjection

data class NoticesResponse @QueryProjection constructor(
    val all: List<MainNoticeResponse>,
    val scholarship: List<MainNoticeResponse>,
    val undergraduate: List<MainNoticeResponse>,
    val graduate: List<MainNoticeResponse>
){
}