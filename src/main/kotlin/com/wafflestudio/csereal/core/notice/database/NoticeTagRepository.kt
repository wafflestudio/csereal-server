package com.wafflestudio.csereal.core.notice.database

import org.springframework.data.jpa.repository.JpaRepository

interface NoticeTagRepository : JpaRepository<NoticeTagEntity, Long> {
    fun findAllByNoticeId(noticeId: Long)
    fun deleteAllByNoticeId(noticeId: Long)
}