package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchType
import org.springframework.stereotype.Component

@Component
class MemberSearchDocumentProvider(
    private val memberSearchRepository: MemberSearchRepository
) : SearchDocumentProvider {
    override fun collectAll(): List<SearchDocument> {
        val rows = memberSearchRepository.findAll()
        return SearchDocument.bilingual(
            rows = rows.filter { it.professor != null },
            type = SearchType.PROFESSOR,
            groupBy = { it.professor!!.professor.id },
            sourceId = { it.professor!!.professor.id },
            language = { it.language },
            title = { it.professor!!.name },
            body = { it.content }
        ) + SearchDocument.bilingual(
            rows = rows.filter { it.staff != null },
            type = SearchType.STAFF,
            groupBy = { it.staff!!.staff.id },
            sourceId = { it.staff!!.staff.id },
            language = { it.language },
            title = { it.staff!!.name },
            body = { it.content }
        )
    }
}
