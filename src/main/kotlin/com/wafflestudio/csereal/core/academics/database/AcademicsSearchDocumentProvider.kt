package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchType
import org.springframework.stereotype.Component

@Component
class AcademicsSearchDocumentProvider(
    private val academicsSearchRepository: AcademicsSearchRepository
) : SearchDocumentProvider {
    override fun collectAll(): List<SearchDocument> {
        val rows = academicsSearchRepository.findAll()
        return SearchDocument.bilingual(
            // 학사 안내와 교과목은 부모가 없다. 화면을 정하는 값들이 사실상 자연키다.
            rows = rows.filter { it.academics != null },
            type = SearchType.ACADEMICS,
            groupBy = { it.academics!!.let { a -> Triple(a.studentType, a.postType, a.year) } },
            sourceId = { it.academics!!.id },
            language = { it.language },
            title = { it.academics!!.name },
            body = { it.content }
        ) + SearchDocument.bilingual(
            rows = rows.filter { it.course != null },
            type = SearchType.COURSE,
            groupBy = { it.course!!.let { c -> c.studentType to c.code } },
            sourceId = { it.course!!.id },
            language = { it.language },
            title = { it.course!!.name },
            body = { it.content }
        ) + SearchDocument.bilingual(
            rows = rows.filter { it.scholarship != null },
            type = SearchType.SCHOLARSHIP,
            groupBy = { it.scholarship!!.scholarship.id },
            sourceId = { it.scholarship!!.scholarship.id },
            language = { it.language },
            title = { it.scholarship!!.name },
            body = { it.content }
        )
    }
}
