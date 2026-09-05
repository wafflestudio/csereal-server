package com.wafflestudio.csereal.core.admissions.database

import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchType
import org.springframework.stereotype.Component

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
        body = { it.searchContent },
        url = { "/admissions/${it.mainType.toValue()}/${it.postType.toValue()}" }
    )
}
