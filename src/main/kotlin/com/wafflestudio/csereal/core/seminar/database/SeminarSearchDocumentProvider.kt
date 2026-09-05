package com.wafflestudio.csereal.core.seminar.database

import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchType
import org.springframework.stereotype.Component

@Component
class SeminarSearchDocumentProvider(
    private val seminarRepository: SeminarRepository
) : SearchDocumentProvider {
    // 지금 MySQL 검색이 보는 일곱 컬럼을 그대로 본문에 담는다(연사·소속·장소 포함).
    override fun collectAll(): List<SearchDocument> = seminarRepository.findAll().map {
        SearchDocument(
            type = SearchType.SEMINAR,
            sourceId = it.id,
            titleKo = it.title,
            titleEn = null,
            bodyKo = listOfNotNull(
                it.name,
                it.affiliation,
                it.location,
                it.plainTextDescription,
                it.plainTextIntroduction,
                it.plainTextAdditionalNote
            ).joinToString("\n"),
            bodyEn = null,
            createdAt = SearchDocument.timestamp(it.startDate),
            isPrivate = it.isPrivate
        )
    }
}
