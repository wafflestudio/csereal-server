package com.wafflestudio.csereal.core.news.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.news.database.*
import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.news.dto.NewsSearchResponse
import com.wafflestudio.csereal.core.news.dto.NewsTotalSearchDto
import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface NewsService {
    fun searchNews(tag: List<String>?, keyword: String?, pageable: Pageable, usePageBtn: Boolean): NewsSearchResponse
    fun readNews(newsId: Long): NewsDto
    fun createNews(request: NewsDto, mainImage: MultipartFile?, attachments: List<MultipartFile>?): NewsDto
    fun updateNews(
        newsId: Long,
        request: NewsDto,
        newMainImage: MultipartFile?,
        newAttachments: List<MultipartFile>?,
        deleteIds: List<Long>,
    ): NewsDto

    fun deleteNews(newsId: Long)
    fun enrollTag(tagName: String)
    fun searchTotalNews(keyword: String, number: Int, amount: Int): NewsTotalSearchDto
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
        pageable: Pageable,
        usePageBtn: Boolean
    ): NewsSearchResponse {
        return newsRepository.searchNews(tag, keyword, pageable, usePageBtn)
    }

    @Transactional(readOnly = true)
    override fun searchTotalNews(
            keyword: String,
            number: Int,
            amount: Int,
    ) = newsRepository.searchTotalNews(
            keyword,
            number,
            amount,
            mainImageService::createImageURL,
    )

    @Transactional(readOnly = true)
    override fun readNews(newsId: Long): NewsDto {
        val news: NewsEntity = newsRepository.findByIdOrNull(newsId)
            ?: throw CserealException.Csereal404("존재하지 않는 새소식입니다.(newsId: $newsId)")

        if (news.isDeleted) throw CserealException.Csereal404("삭제된 새소식입니다.(newsId: $newsId)")

        val imageURL = mainImageService.createImageURL(news.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(news.attachments)

        val prevNews = newsRepository.findFirstByCreatedAtLessThanOrderByCreatedAtDesc(news.createdAt!!)
        val nextNews = newsRepository.findFirstByCreatedAtGreaterThanOrderByCreatedAtAsc(news.createdAt!!)

        return NewsDto.of(news, imageURL, attachmentResponses, prevNews, nextNews)
    }

    @Transactional
    override fun createNews(request: NewsDto, mainImage: MultipartFile?, attachments: List<MultipartFile>?): NewsDto {
        val newNews = NewsEntity.of(request)

        for (tag in request.tags) {
            val tagEnum = TagInNewsEnum.getTagEnum(tag)
            val tagEntity = tagInNewsRepository.findByName(tagEnum)
            NewsTagEntity.createNewsTag(newNews, tagEntity)
        }

        if (mainImage != null) {
            mainImageService.uploadMainImage(newNews, mainImage)
        }

        if (attachments != null) {
            attachmentService.uploadAllAttachments(newNews, attachments)
        }

        newsRepository.save(newNews)

        val imageURL = mainImageService.createImageURL(newNews.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(newNews.attachments)

        return NewsDto.of(newNews, imageURL, attachmentResponses)
    }

    @Transactional
    override fun updateNews(
        newsId: Long,
        request: NewsDto,
        newMainImage: MultipartFile?,
        newAttachments: List<MultipartFile>?,
        deleteIds: List<Long>,
    ): NewsDto {
        val news: NewsEntity = newsRepository.findByIdOrNull(newsId)
            ?: throw CserealException.Csereal404("존재하지 않는 새소식입니다. (newsId: $newsId)")
        if (news.isDeleted) throw CserealException.Csereal404("삭제된 새소식입니다.")

        news.update(request)

        if (newMainImage != null) {
            news.mainImage?.isDeleted = true
            mainImageService.uploadMainImage(news, newMainImage)
        }

        attachmentService.deleteAttachments(deleteIds)

        if (newAttachments != null) {
            attachmentService.uploadAllAttachments(news, newAttachments)
        }

        val oldTags = news.newsTags.map { it.tag.name }

        val tagsToRemove = oldTags - request.tags.map { TagInNewsEnum.getTagEnum(it) }
        val tagsToAdd = request.tags.map { TagInNewsEnum.getTagEnum(it) } - oldTags

        for (tagEnum in tagsToRemove) {
            val tagId = tagInNewsRepository.findByName(tagEnum).id
            news.newsTags.removeIf { it.tag.name == tagEnum }
            newsTagRepository.deleteByNewsIdAndTagId(newsId, tagId)
        }

        for (tagEnum in tagsToAdd) {
            val tagId = tagInNewsRepository.findByName(tagEnum)
            NewsTagEntity.createNewsTag(news, tagId)
        }

        val imageURL = mainImageService.createImageURL(news.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(news.attachments)

        return NewsDto.of(news, imageURL, attachmentResponses)
    }

    @Transactional
    override fun deleteNews(newsId: Long) {
        val news: NewsEntity = newsRepository.findByIdOrNull(newsId)
            ?: throw CserealException.Csereal404("존재하지 않는 새소식입니다.(newsId: $newsId")

        news.isDeleted = true
    }

    override fun enrollTag(tagName: String) {
        val newTag = TagInNewsEntity(
            name = TagInNewsEnum.getTagEnum(tagName)
        )
        tagInNewsRepository.save(newTag)
    }
}
