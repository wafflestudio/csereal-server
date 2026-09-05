package com.wafflestudio.csereal.core.news.database

import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchType
import org.springframework.stereotype.Component

@Component
class NewsSearchDocumentProvider(
    private val newsRepository: NewsRepository,
    private val mainImageService: MainImageService
) : SearchDocumentProvider {
    override fun collectAll(): List<SearchDocument> = newsRepository.findAll().map(::toDocument)

    // 만 건이 넘어 기본 구현(전부 만들어 걸러내기)을 쓰면 글 하나 저장할 때마다 전량을 읽는다.
    override fun collectOne(type: SearchType, sourceId: Long): SearchDocument? =
        if (type != SearchType.NEWS) {
            null
        } else {
            newsRepository.findById(sourceId).orElse(null)?.let(::toDocument)
        }

    private fun toDocument(news: NewsEntity) = SearchDocument(
        type = SearchType.NEWS,
        sourceId = news.id,
        titleKo = news.title,
        titleEn = null,
        bodyKo = news.plainTextDescription,
        bodyEn = null,
        url = "/community/news/${news.id}",
        thumbnailUrl = mainImageService.createImageURL(news.mainImage),
        // 목록·상세가 보여주고 정렬 기준으로 쓰는 건 글이 생긴 시각이 아니라 이 date 다.
        createdAt = SearchDocument.timestamp(news.date),
        isPrivate = news.isPrivate,
        tags = news.newsTags.map { it.tag.name.name }
    )
}
