package com.wafflestudio.csereal.core.notice.database

import org.springframework.data.jpa.repository.JpaRepository

interface NoticeRepository : JpaRepository<NoticeEntity, Long> {
}