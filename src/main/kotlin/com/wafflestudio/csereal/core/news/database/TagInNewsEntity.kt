package com.wafflestudio.csereal.core.news.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity(name = "tag_in_news")
class TagInNewsEntity(
    var name: String,

    @OneToMany(mappedBy = "tag")
    val newsTags: MutableSet<NewsTagEntity> = mutableSetOf()
) : BaseTimeEntity() {
}