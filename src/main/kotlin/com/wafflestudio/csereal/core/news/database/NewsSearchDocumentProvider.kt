package com.wafflestudio.csereal.core.news.database

import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchType
import org.springframework.stereotype.Component

@Component
class NewsSearchDocumentProvider(
    private val newsRepository: NewsRepository
) : SearchDocumentProvider {
    override fun collectAll(): List<SearchDocument> = newsRepository.findAll().map {
        SearchDocument(
            type = SearchType.NEWS,
            sourceId = it.id,
            titleKo = it.title,
            titleEn = null,
            bodyKo = it.plainTextDescription,
            bodyEn = null,
            createdAt = SearchDocument.timestamp(it.createdAt),
            isPrivate = it.isPrivate
        )
    }
}
