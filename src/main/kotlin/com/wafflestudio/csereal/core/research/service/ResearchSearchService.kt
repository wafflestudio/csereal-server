package com.wafflestudio.csereal.core.research.service

import com.wafflestudio.csereal.core.research.database.ResearchSearchEntity
import com.wafflestudio.csereal.core.research.database.ResearchSearchRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface ResearchSearchService {
    fun deleteResearchSearch(researchSearchEntity: ResearchSearchEntity)
}

@Service
class ResearchSearchServiceImpl (
    private val researchSearchRepository: ResearchSearchRepository,
) : ResearchSearchService {

    @Transactional
    override fun deleteResearchSearch(
            researchSearchEntity: ResearchSearchEntity
    ) {
        researchSearchRepository.delete(researchSearchEntity)
    }
}