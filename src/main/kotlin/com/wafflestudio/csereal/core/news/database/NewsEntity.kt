package com.wafflestudio.csereal.core.news.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.resource.image.database.ImageEntity
import jakarta.persistence.*

@Entity(name = "news")
class NewsEntity(

    var isDeleted: Boolean = false,

    var title: String,

    var description: String,

    var isPublic: Boolean,

    var isSlide: Boolean,

    // 새소식 작성란에도 "가장 위에 표시"가 있더라고요, 혹시 쓸지도 모르니까 남겼습니다
    var isPinned: Boolean,

    @OneToOne(mappedBy = "news", cascade = [CascadeType.ALL])
    var mainImage: ImageEntity?,

    @OneToMany(mappedBy = "news", cascade = [CascadeType.ALL])
    var newsTags: MutableSet<NewsTagEntity> = mutableSetOf()

): BaseTimeEntity() {
    companion object {
        fun of(newsDto: NewsDto, imageEntity: ImageEntity?): NewsEntity {
            return NewsEntity(
                title = newsDto.title,
                description = newsDto.description,
                isPublic = newsDto.isPublic,
                isSlide = newsDto.isSlide,
                isPinned = newsDto.isPinned,
                mainImage = imageEntity,
            )
        }
    }
    fun update(updateNewsRequest: NewsDto) {
        this.title = updateNewsRequest.title
        this.description = updateNewsRequest.description
        this.isPublic = updateNewsRequest.isPublic
        this.isSlide = updateNewsRequest.isSlide
        this.isPinned = updateNewsRequest.isPinned
    }
}