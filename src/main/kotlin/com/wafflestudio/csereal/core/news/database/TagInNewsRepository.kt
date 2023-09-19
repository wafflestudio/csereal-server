package com.wafflestudio.csereal.core.news.database

import org.springframework.data.jpa.repository.JpaRepository

interface TagInNewsRepository : JpaRepository<TagInNewsEntity, Long> {
    fun findByName(tagName: TagInNewsEnum): TagInNewsEntity
}
