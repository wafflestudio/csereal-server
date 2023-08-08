package com.wafflestudio.csereal.core.news.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.news.database.*
import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.news.dto.NewsSearchResponse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface NewsService {
    fun searchNews(tag: List<String>?, keyword: String?, pageNum: Long): NewsSearchResponse
    fun readNews(newsId: Long, tag: List<String>?, keyword: String?): NewsDto
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

    @Transactional(readOnly = true)
    override fun readNews(
        newsId: Long,
        tag: List<String>?,
        keyword: String?
    ): NewsDto {
        val news: NewsEntity = newsRepository.findByIdOrNull(newsId)
            ?: throw CserealException.Csereal404("존재하지 않는 새소식입니다.(newsId: $newsId)")

        if (news.isDeleted) throw CserealException.Csereal404("삭제된 새소식입니다.(newsId: $newsId")

        val prevNext = newsRepository.findPrevNextId(newsId, tag, keyword)


        return NewsDto.of(news, prevNext)
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

        return NewsDto.of(newNews, null)
    }

    @Transactional
    override fun updateNews(newsId: Long, request: NewsDto): NewsDto {
        val news: NewsEntity = newsRepository.findByIdOrNull(newsId)
            ?: throw CserealException.Csereal404("존재하지 않는 새소식입니다. (newsId: $newsId)")
        if (news.isDeleted) throw CserealException.Csereal404("삭제된 새소식입니다.")
        news.update(request)

        val oldTags = news.newsTags.map { it.tag.name }

        val tagsToRemove = oldTags - request.tags
        val tagsToAdd = request.tags - oldTags

        for(tagName in tagsToRemove) {
            val tagId = tagInNewsRepository.findByName(tagName)!!.id
            news.newsTags.removeIf { it.tag.name == tagName }
            newsTagRepository.deleteByNewsIdAndTagId(newsId, tagId)
        }

        for (tagName in tagsToAdd) {
            val tag = tagInNewsRepository.findByName(tagName) ?: throw CserealException.Csereal404("해당하는 태그가 없습니다")
            NewsTagEntity.createNewsTag(news,tag)
        }

        return NewsDto.of(news, null)
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