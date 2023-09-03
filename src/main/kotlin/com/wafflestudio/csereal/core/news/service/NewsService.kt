package com.wafflestudio.csereal.core.news.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.news.database.*
import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.news.dto.NewsSearchResponse
import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface NewsService {
    fun searchNews(tag: List<String>?, keyword: String?, pageNum: Long): NewsSearchResponse
    fun readNews(newsId: Long, tag: List<String>?, keyword: String?): NewsDto
    fun createNews(request: NewsDto, mainImage: MultipartFile?, attachments: List<MultipartFile>?): NewsDto
    fun updateNews(newsId: Long, request: NewsDto, mainImage: MultipartFile?, attachments: List<MultipartFile>?): NewsDto
    fun deleteNews(newsId: Long)
    fun enrollTag(tagName: String)
}

@Service
class NewsServiceImpl(
    private val newsRepository: NewsRepository,
    private val tagInNewsRepository: TagInNewsRepository,
    private val newsTagRepository: NewsTagRepository,
    private val mainImageService: MainImageService,
    private val attachmentService: AttachmentService,
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

        val imageURL = mainImageService.createImageURL(news.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(news.attachments)

        val prevNext = newsRepository.findPrevNextId(newsId, tag, keyword)
            ?: throw CserealException.Csereal400("이전글 다음글이 존재하지 않습니다.(newsId=$newsId)")

        return NewsDto.of(news, imageURL, attachmentResponses, prevNext)
    }

    @Transactional
    override fun createNews(request: NewsDto, mainImage: MultipartFile?, attachments: List<MultipartFile>?): NewsDto {
        val newNews = NewsEntity.of(request)

        for (tagName in request.tags) {
            val tag = tagInNewsRepository.findByName(tagName) ?: throw CserealException.Csereal404("해당하는 태그가 없습니다")
            NewsTagEntity.createNewsTag(newNews, tag)
        }

        if(mainImage != null) {
            mainImageService.uploadMainImage(newNews, mainImage)
        }

        if(attachments != null) {
            attachmentService.uploadAllAttachments(newNews, attachments)
        }

        newsRepository.save(newNews)

        val imageURL = mainImageService.createImageURL(newNews.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(newNews.attachments)

        return NewsDto.of(newNews, imageURL, attachmentResponses, null)
    }

    @Transactional
    override fun updateNews(newsId: Long, request: NewsDto, mainImage: MultipartFile?, attachments: List<MultipartFile>?): NewsDto {
        val news: NewsEntity = newsRepository.findByIdOrNull(newsId)
            ?: throw CserealException.Csereal404("존재하지 않는 새소식입니다. (newsId: $newsId)")
        if (news.isDeleted) throw CserealException.Csereal404("삭제된 새소식입니다.")
        news.update(request)

        if(mainImage != null) {
            mainImageService.uploadMainImage(news, mainImage)
        } else {
            news.mainImage = null
        }

        if(attachments != null) {
            news.attachments.clear()
            attachmentService.uploadAllAttachments(news, attachments)
        } else {
            news.attachments.clear()
        }

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

        val imageURL = mainImageService.createImageURL(news.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(news.attachments)

        return NewsDto.of(news, imageURL, attachmentResponses, null)
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