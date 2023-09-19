package com.wafflestudio.csereal.core.notice.database

import org.springframework.data.jpa.repository.JpaRepository

interface TagInNoticeRepository : JpaRepository<TagInNoticeEntity, Long> {
    fun findByName(tagName: TagInNoticeEnum): TagInNoticeEntity
}
