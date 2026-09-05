package com.wafflestudio.csereal.core.notice.database

import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchDomain
import org.springframework.stereotype.Component

@Component
class NoticeSearchDocumentProvider(
    private val noticeRepository: NoticeRepository
) : SearchDocumentProvider {
    override val domain = SearchDomain.NOTICE

    // 공지는 번역본이 없다. 한국어 필드만 채우면 한/영 화면 어디서든 걸린다.
    override fun collectAll(): List<SearchDocument> = noticeRepository.findAll().map {
        SearchDocument(
            domain = domain,
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
