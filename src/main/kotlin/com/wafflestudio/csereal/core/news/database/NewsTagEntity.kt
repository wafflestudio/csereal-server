package com.wafflestudio.csereal.core.news.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity(name = "newsTag")
class NewsTagEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id")
    val news: NewsEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    var tag: TagInNewsEntity

) : BaseTimeEntity() {

    companion object {
        fun createNewsTag(news: NewsEntity, tag: TagInNewsEntity) {
            val newsTag = NewsTagEntity(news, tag)
            news.newsTags.add(newsTag)
            tag.newsTags.add(newsTag)
        }
    }
}
