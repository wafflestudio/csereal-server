package com.wafflestudio.csereal.core.news.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.admin.dto.AdminSlidesResponse
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
    fun searchNews(
        tag: List<String>?,
        keyword: String?,
        pageable: Pageable,
        usePageBtn: Boolean,
        isStaff: Boolean
    ): NewsSearchResponse

    fun readNews(newsId: Long, isStaff: Boolean): NewsDto
    fun createNews(request: NewsDto, mainImage: MultipartFile?, attachments: List<MultipartFile>?): NewsDto
    fun updateNews(
        newsId: Long,
        request: NewsDto,
        newMainImage: MultipartFile?,
        newAttachments: List<MultipartFile>?
    ): NewsDto

    fun deleteNews(newsId: Long)
    fun enrollTag(tagName: String)
    fun searchTotalNews(keyword: String, number: Int, amount: Int): NewsTotalSearchDto
    fun readAllSlides(pageNum: Long, pageSize: Int): AdminSlidesResponse
    fun unSlideManyNews(request: List<Long>)
}

@Service
class NewsServiceImpl(
    private val newsRepository: NewsRepository,
    private val tagInNewsRepository: TagInNewsRepository,
    private val newsTagRepository: NewsTagRepository,
    private val mainImageService: MainImageService,
    private val attachmentService: AttachmentService
) : NewsService {
    @Transactional(readOnly = true)
    override fun searchNews(
        tag: List<String>?,
        keyword: String?,
        pageable: Pageable,
        usePageBtn: Boolean,
        isStaff: Boolean
    ): NewsSearchResponse {
        return newsRepository.searchNews(tag, keyword, pageable, usePageBtn, isStaff)
    }

    @Transactional(readOnly = true)
    override fun searchTotalNews(
        keyword: String,
        number: Int,
        amount: Int
    ) = newsRepository.searchTotalNews(
        keyword,
        number,
        amount,
        mainImageService::createImageURL
    )

    @Transactional(readOnly = true)
    override fun readNews(newsId: Long, isStaff: Boolean): NewsDto {
        val news: NewsEntity = newsRepository.findByIdOrNull(newsId)
            ?: throw CserealException.Csereal404("존재하지 않는 새소식입니다.(newsId: $newsId)")

        if (news.isDeleted) throw CserealException.Csereal404("삭제된 새소식입니다.(newsId: $newsId)")

        if (news.isPrivate && !isStaff) throw CserealException.Csereal401("접근 권한이 없습니다.")

        val imageURL = mainImageService.createImageURL(news.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(news.attachments)

        val prevNews =
            newsRepository.findFirstByIsDeletedFalseAndIsPrivateFalseAndCreatedAtLessThanOrderByCreatedAtDesc(
                news.createdAt!!
            )
        val nextNews =
            newsRepository.findFirstByIsDeletedFalseAndIsPrivateFalseAndCreatedAtGreaterThanOrderByCreatedAtAsc(
                news.createdAt!!
            )

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
        newAttachments: List<MultipartFile>?
    ): NewsDto {
        val news: NewsEntity = getNewsEntityByIdOrThrow(newsId)
        if (news.isDeleted) throw CserealException.Csereal404("삭제된 새소식입니다.")

        news.update(request)

        if (newMainImage != null) {
            news.mainImage?.isDeleted = true
            mainImageService.uploadMainImage(news, newMainImage)
        }

        attachmentService.deleteAttachments(request.deleteIds)

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
        val news: NewsEntity = getNewsEntityByIdOrThrow(newsId)

        news.isDeleted = true
    }

    override fun enrollTag(tagName: String) {
        val newTag = TagInNewsEntity(
            name = TagInNewsEnum.getTagEnum(tagName)
        )
        tagInNewsRepository.save(newTag)
    }

    @Transactional(readOnly = true)
    override fun readAllSlides(pageNum: Long, pageSize: Int): AdminSlidesResponse {
        return newsRepository.readAllSlides(pageNum, pageSize)
    }

    @Transactional
    override fun unSlideManyNews(request: List<Long>) {
        for (newsId in request) {
            val news = getNewsEntityByIdOrThrow(newsId)
            news.isSlide = false
        }
    }

    fun getNewsEntityByIdOrThrow(newsId: Long): NewsEntity {
        return newsRepository.findByIdOrNull(newsId)
            ?: throw CserealException.Csereal404("존재하지 않는 새소식입니다.(newsId: $newsId)")
    }
}
