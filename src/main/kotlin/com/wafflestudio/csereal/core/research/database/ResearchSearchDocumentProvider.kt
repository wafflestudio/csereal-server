package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchType
import com.wafflestudio.csereal.core.conference.database.ConferenceRepository
import org.springframework.stereotype.Component

@Component
class ResearchSearchDocumentProvider(
    private val researchRepository: ResearchRepository,
    private val labRepository: LabRepository,
    private val conferenceRepository: ConferenceRepository
) : SearchDocumentProvider {
    override fun collectAll(): List<SearchDocument> = SearchDocument.bilingual(
        rows = researchRepository.findAll().flatMap { it.translations },
        type = SearchType.RESEARCH,
        groupBy = { it.research.id },
        sourceId = { it.research.id },
        language = { it.language },
        title = { it.name },
        body = { ResearchSearchEntity.createContent(it) }
    ) + SearchDocument.bilingual(
        rows = labRepository.findAll().flatMap { it.translations },
        type = SearchType.LAB,
        groupBy = { it.lab.id },
        sourceId = { it.lab.id },
        language = { it.language },
        title = { it.name },
        body = { ResearchSearchEntity.createContent(it) }
    ) + SearchDocument.bilingual(
        // 학회는 부모가 없다. 약칭이 한/영 공통이라 그걸로 묶는다.
        rows = conferenceRepository.findAll(),
        type = SearchType.CONFERENCE,
        groupBy = { it.abbreviation },
        sourceId = { it.id },
        language = { it.language },
        title = { it.name },
        body = { ResearchSearchEntity.createContent(it) }
    )
}
