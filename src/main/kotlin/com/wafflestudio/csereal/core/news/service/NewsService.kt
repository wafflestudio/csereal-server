package com.wafflestudio.csereal.core.news.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.news.database.*
import com.wafflestudio.csereal.core.news.dto.CreateNewsRequest
import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.news.dto.UpdateNewsRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface NewsService {
    fun readNews(newsId: Long): NewsDto
    fun createNews(request: CreateNewsRequest): NewsDto
    fun updateNews(newsId: Long, request: UpdateNewsRequest): NewsDto
    fun deleteNews(newsId: Long)
    fun enrollTag(tagName: String)
}

@Service
class NewsServiceImpl(
    private val newsRepository: NewsRepository,
    private val tagInNewsRepository: TagInNewsRepository,
    private val newsTagRepository: NewsTagRepository
) : NewsService {

    @Transactional
    override fun readNews(newsId: Long): NewsDto {
        val news: NewsEntity = newsRepository.findByIdOrNull(newsId)
            ?: throw CserealException.Csereal404("존재하지 않는 새소식입니다.(newsId: $newsId)")

        if (news.isDeleted) throw CserealException.Csereal404("삭제된 새소식입니다.(newsId: $newsId")
        return NewsDto.of(news)
    }

    @Transactional
    override fun createNews(request: CreateNewsRequest): NewsDto {
        val newNews = NewsEntity(
            title = request.title,
            description = request.description,
            isPublic = request.isPublic,
            isSlide = request.isSlide,
            isPinned = request.isPinned
        )

        for (tagId in request.tags) {
            val tag = tagInNewsRepository.findByIdOrNull(tagId) ?: throw CserealException.Csereal400("해당하는 태그가 없습니다")
            NewsTagEntity.createNewsTag(newNews, tag)
        }

        newsRepository.save(newNews)

        return NewsDto.of(newNews)
    }

    @Transactional
    override fun updateNews(newsId: Long, request: UpdateNewsRequest): NewsDto {
        val news: NewsEntity = newsRepository.findByIdOrNull(newsId)
            ?: throw CserealException.Csereal400("존재하지 않는 새소식입니다. (newsId: $newsId)")
        if (news.isDeleted) throw CserealException.Csereal400("삭제된 새소식입니다.")
        news.title = request.title ?: news.title
        news.description = request.description ?: news.description
        news.isPublic = request.isPublic ?: news.isPublic
        news.isSlide = request.isSlide ?: news.isSlide
        news.isPinned = request.isPinned ?: news.isPinned

        if(request.tags != null) {
            newsTagRepository.deleteAllByNewsId(newsId)
            news.newsTags.clear()
            for (tagId in request.tags) {
                val tag = tagInNewsRepository.findByIdOrNull(tagId) ?: throw CserealException.Csereal404("해당하는 태그가 없습니다")
                NewsTagEntity.createNewsTag(news, tag)
            }
        }
        return NewsDto.of(news)
    }

    @Transactional
    override fun deleteNews(newsId: Long) {
        val news: NewsEntity = newsRepository.findByIdOrNull(newsId)
            ?: throw CserealException.Csereal404("존재하지 않는 새소식입니다.(newsId: $newsId")

        news.isDeleted = true
    }

    override fun enrollTag(tagName: String) {
        val newTag = TagInNewsEntity(
            name = tagName
        )
        tagInNewsRepository.save(newTag)
    }
}