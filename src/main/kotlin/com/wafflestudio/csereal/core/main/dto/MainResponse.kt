package com.wafflestudio.csereal.core.main.dto

data class MainResponse(
    val slide: List<NewsResponse>,
    val noticeTotal: List<NoticeResponse>,
    val noticeAdmissions: List<NoticeResponse>,
    val noticeUndergraduate: List<NoticeResponse>,
    val noticeGraduate: List<NoticeResponse>
) {

}