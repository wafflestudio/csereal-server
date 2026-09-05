package com.wafflestudio.csereal.core.notice.database

import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchType
import org.springframework.stereotype.Component

@Component
class NoticeSearchDocumentProvider(
    private val noticeRepository: NoticeRepository
) : SearchDocumentProvider {
    override fun collectAll(): List<SearchDocument> = noticeRepository.findAll().map(::toDocument)

    // 만 건이 넘어 기본 구현(전부 만들어 걸러내기)을 쓰면 글 하나 저장할 때마다 전량을 읽는다.
    override fun collectOne(type: SearchType, sourceId: Long): SearchDocument? =
        if (type != SearchType.NOTICE) {
            null
        } else {
            noticeRepository.findById(sourceId).orElse(null)?.let(::toDocument)
        }

    // 공지는 번역본이 없다. 한국어 필드만 채우면 한/영 화면 어디서든 걸린다.
    private fun toDocument(notice: NoticeEntity) = SearchDocument(
        type = SearchType.NOTICE,
        sourceId = notice.id,
        titleKo = notice.title,
        titleEn = null,
        bodyKo = notice.plainTextDescription,
        bodyEn = null,
        url = "/community/notice/${notice.id}",
        createdAt = SearchDocument.timestamp(notice.createdAt),
        isPrivate = notice.isPrivate
    )
}
