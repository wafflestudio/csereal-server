package com.wafflestudio.csereal.core.news.database

import org.springframework.data.jpa.repository.JpaRepository

interface NewsTagRepository : JpaRepository<NewsTagEntity, Long> {
    fun deleteAllByNewsId(newsId: Long)
    fun deleteByNewsIdAndTagId(newsId: Long, tagId: Long)
}
