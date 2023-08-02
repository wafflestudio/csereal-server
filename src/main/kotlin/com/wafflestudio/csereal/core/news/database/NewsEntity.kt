package com.wafflestudio.csereal.core.news.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.news.dto.NewsDto
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity(name = "news")
class NewsEntity(

    @Column
    var isDeleted: Boolean = false,

    @Column
    var title: String,

    @Column(columnDefinition = "text")
    var description: String,

    var isPublic: Boolean,

    var isSlide: Boolean,

    // 새소식 작성란에도 "가장 위에 표시"가 있더라고요, 혹시 쓸지도 모르니까 남겼습니다
    var isPinned: Boolean,

    @OneToMany(mappedBy = "news", cascade = [CascadeType.ALL])
    var newsTags: MutableSet<NewsTagEntity> = mutableSetOf()

): BaseTimeEntity() {
    fun update(updateNewsRequest: NewsDto) {
        this.title = updateNewsRequest.title
        this.description = updateNewsRequest.description
        this.isPublic = updateNewsRequest.isPublic
        this.isSlide = updateNewsRequest.isSlide
        this.isPinned = updateNewsRequest.isPinned
    }
}