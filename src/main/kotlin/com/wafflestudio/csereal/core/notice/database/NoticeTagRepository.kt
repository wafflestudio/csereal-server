package com.wafflestudio.csereal.core.notice.database

import org.springframework.data.jpa.repository.JpaRepository

interface NoticeTagRepository : JpaRepository<NoticeTagEntity, Long> {
    fun deleteAllByNoticeId(noticeId: Long)
    fun deleteByNoticeIdAndTagId(noticeId: Long, tagId: Long)
}