package com.wafflestudio.csereal.core.about.database

import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchType
import org.springframework.stereotype.Component

@Component
class AboutSearchDocumentProvider(
    private val aboutTranslationRepository: AboutTranslationRepository
) : SearchDocumentProvider {
    override fun collectAll(): List<SearchDocument> = SearchDocument.bilingual(
        rows = aboutTranslationRepository.findAll(),
        type = SearchType.ABOUT,
        groupBy = { it.about.id },
        sourceId = { it.about.id },
        language = { it.language },
        title = { it.name },
        body = { it.searchContent },
        url = { "/about/${it.about.postType.toValue()}" }
    )
}
