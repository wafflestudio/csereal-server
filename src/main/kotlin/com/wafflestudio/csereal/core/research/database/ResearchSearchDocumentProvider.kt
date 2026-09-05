package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchType
import org.springframework.stereotype.Component

@Component
class ResearchSearchDocumentProvider(
    private val researchSearchRepository: ResearchSearchRepository
) : SearchDocumentProvider {
    override fun collectAll(): List<SearchDocument> {
        val rows = researchSearchRepository.findAll()
        return SearchDocument.bilingual(
            rows = rows.filter { it.research != null },
            type = SearchType.RESEARCH,
            groupBy = { it.research!!.research.id },
            sourceId = { it.research!!.research.id },
            language = { it.language },
            title = { it.research!!.name },
            body = { it.content }
        ) + SearchDocument.bilingual(
            rows = rows.filter { it.lab != null },
            type = SearchType.LAB,
            groupBy = { it.lab!!.lab.id },
            sourceId = { it.lab!!.lab.id },
            language = { it.language },
            title = { it.lab!!.name },
            body = { it.content }
        ) + SearchDocument.bilingual(
            // 학회는 부모가 없다. 약칭이 한/영 공통이라 그걸로 묶는다.
            rows = rows.filter { it.conferenceElement != null },
            type = SearchType.CONFERENCE,
            groupBy = { it.conferenceElement!!.abbreviation },
            sourceId = { it.conferenceElement!!.id },
            language = { it.language },
            title = { it.conferenceElement!!.name },
            body = { it.content }
        )
    }
}
