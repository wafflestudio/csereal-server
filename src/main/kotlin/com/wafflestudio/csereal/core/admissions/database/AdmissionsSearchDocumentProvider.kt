package com.wafflestudio.csereal.core.admissions.database

import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchType
import org.springframework.stereotype.Component
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml

@Component
class AdmissionsSearchDocumentProvider(
    private val admissionsRepository: AdmissionsRepository
) : SearchDocumentProvider {
    // 입학 안내는 부모 테이블이 없다. (mainType, postType) 이 사실상 부모라 그걸로 묶는다.
    override fun collectAll(): List<SearchDocument> = SearchDocument.bilingual(
        rows = admissionsRepository.findAll(),
        type = SearchType.ADMISSIONS,
        groupBy = { it.mainType to it.postType },
        sourceId = { it.id },
        language = { it.language },
        title = { it.name },
        body = { searchTextOf(it) },
        url = { "/admissions/${it.mainType.toValue()}/${it.postType.toValue()}" }
    )
}

/** 검색 대상 텍스트. 본문은 HTML 이라 태그를 걷어내고, 분류 이름도 같은 언어판으로 담는다. */
private fun searchTextOf(admissions: AdmissionsEntity) = StringBuilder().apply {
    appendLine(admissions.name)
    appendLine(admissions.mainType.getLanguageValue(admissions.language))
    appendLine(admissions.postType.getLanguageValue(admissions.language))
    appendLine(cleanTextFromHtml(admissions.description))
}.toString()
