package com.wafflestudio.csereal.core.news.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.news.database.*
import com.wafflestudio.csereal.core.news.dto.CreateNewsRequest
import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.news.dto.NewsSearchResponse
import com.wafflestudio.csereal.core.news.dto.UpdateNewsRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface NewsService {
    fun searchNews(tag: List<String>?, keyword: String?, pageNum: Long): NewsSearchResponse
    fun readNews(newsId: Long): NewsDto
    fun createNews(request: NewsDto): NewsDto
    fun updateNews(newsId: Long, request: NewsDto): NewsDto
    fun deleteNews(newsId: Long)
    fun enrollTag(tagName: String)
}

@Service
class NewsServiceImpl(
    private val newsRepository: NewsRepository,
    private val tagInNewsRepository: TagInNewsRepository,
    private val newsTagRepository: NewsTagRepository
) : NewsService {
    @Transactional(readOnly = true)
    override fun searchNews(
        tag: List<String>?,
        keyword: String?,
        pageNum: Long
    ): NewsSearchResponse {
        return newsRepository.searchNews(tag, keyword, pageNum)
    }

    @Transactional
    override fun readNews(newsId: Long): NewsDto {
        val news: NewsEntity = newsRepository.findByIdOrNull(newsId)
            ?: throw CserealException.Csereal404("존재하지 않는 새소식입니다.(newsId: $newsId)")

        if (news.isDeleted) throw CserealException.Csereal404("삭제된 새소식입니다.(newsId: $newsId")
        return NewsDto.of(news)
    }

    @Transactional
    override fun createNews(request: NewsDto): NewsDto {
        val newNews = NewsEntity(
            title = request.title,
            description = request.description,
            isPublic = request.isPublic,
            isSlide = request.isSlide,
            isPinned = request.isPinned
        )

        for (tagName in request.tags) {
            val tag = tagInNewsRepository.findByName(tagName) ?: throw CserealException.Csereal404("해당하는 태그가 없습니다")
            NewsTagEntity.createNewsTag(newNews, tag)
        }

        newsRepository.save(newNews)

        return NewsDto.of(newNews)
    }

    @Transactional
    override fun updateNews(newsId: Long, request: NewsDto): NewsDto {
        val news: NewsEntity = newsRepository.findByIdOrNull(newsId)
            ?: throw CserealException.Csereal404("존재하지 않는 새소식입니다. (newsId: $newsId)")
        if (news.isDeleted) throw CserealException.Csereal404("삭제된 새소식입니다.")
        news.update(request)

        newsTagRepository.deleteAllByNewsId(newsId)

        news.newsTags = news.newsTags.filter { request.tags.contains(it.tag.name) }.toMutableSet()
        for (tagName in request.tags) {
            if(!news.newsTags.map { it.tag.name }.contains(tagName)) {
                val tag = tagInNewsRepository.findByName(tagName) ?: throw CserealException.Csereal404("해당하는 태그가 없습니다")
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