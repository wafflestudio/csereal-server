package com.wafflestudio.csereal.core.seminar.database

import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchType
import org.springframework.stereotype.Component

@Component
class SeminarSearchDocumentProvider(
    private val seminarRepository: SeminarRepository,
    private val mainImageService: MainImageService
) : SearchDocumentProvider {
    override fun collectAll(): List<SearchDocument> = seminarRepository.findAll().map(::toDocument)

    override fun collectOne(type: SearchType, sourceId: Long): SearchDocument? =
        if (type != SearchType.SEMINAR) {
            null
        } else {
            seminarRepository.findById(sourceId).orElse(null)?.let(::toDocument)
        }

    // 지금 MySQL 검색이 보는 일곱 컬럼을 그대로 본문에 담는다(연사·소속·장소 포함).
    private fun toDocument(seminar: SeminarEntity) = SearchDocument(
        type = SearchType.SEMINAR,
        sourceId = seminar.id,
        titleKo = seminar.title,
        titleEn = null,
        bodyKo = listOfNotNull(
            seminar.name,
            seminar.affiliation,
            seminar.location,
            seminar.plainTextDescription,
            seminar.plainTextIntroduction,
            seminar.plainTextAdditionalNote
        ).joinToString("\n"),
        bodyEn = null,
        url = "/community/seminar/${seminar.id}",
        thumbnailUrl = mainImageService.createImageURL(seminar.mainImage),
        createdAt = SearchDocument.timestamp(seminar.startDate),
        isPrivate = seminar.isPrivate
    )
}
