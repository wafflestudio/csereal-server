package com.wafflestudio.csereal.core.notice.database

import org.springframework.data.jpa.repository.JpaRepository

interface TagRepository : JpaRepository<TagEntity, Long> {
}