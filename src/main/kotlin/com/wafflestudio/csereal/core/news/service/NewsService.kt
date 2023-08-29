package com.wafflestudio.csereal.core.news.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.news.database.*
import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.news.dto.NewsSearchResponse
import com.wafflestudio.csereal.core.resource.mainImage.service.ImageService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface NewsService {
    fun searchNews(tag: List<String>?, keyword: String?, pageNum: Long): NewsSearchResponse
    fun readNews(newsId: Long, tag: List<String>?, keyword: String?): NewsDto
    fun createNews(request: NewsDto, image: MultipartFile?): NewsDto
    fun updateNews(newsId: Long, request: NewsDto): NewsDto
    fun deleteNews(newsId: Long)
    fun enrollTag(tagName: String)
}

@Service
class NewsServiceImpl(
    private val newsRepository: NewsRepository,
    private val tagInNewsRepository: TagInNewsRepository,
    private val newsTagRepository: NewsTagRepository,
    private val imageService: ImageService,
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

        if (news.isDeleted) throw CserealException.Csereal404("삭제된 새소식입니다.(newsId: $newsId)")

        val imageURL = imageService.createImageURL(news.mainImage)

        val prevNext = newsRepository.findPrevNextId(newsId, tag, keyword)
            ?: throw CserealException.Csereal400("이전글 다음글이 존재하지 않습니다.(newsId=$newsId)")

        return NewsDto.of(news, imageURL, prevNext)
    }

    @Transactional
    override fun createNews(request: NewsDto, image: MultipartFile?): NewsDto {
        val newNews = NewsEntity.of(request)

        for (tagName in request.tags) {
            val tag = tagInNewsRepository.findByName(tagName) ?: throw CserealException.Csereal404("해당하는 태그가 없습니다")
            NewsTagEntity.createNewsTag(newNews, tag)
        }

        if(image != null) {
            imageService.uploadImage(newNews, image)
        }

        newsRepository.save(newNews)

        val imageURL = imageService.createImageURL(newNews.mainImage)

        return NewsDto.of(newNews, imageURL, null)
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

        val imageURL = imageService.createImageURL(news.mainImage)


        return NewsDto.of(news, imageURL, null)
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