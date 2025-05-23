package com.wafflestudio.csereal.core.research.service

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.conference.database.ConferenceRepository
import com.wafflestudio.csereal.core.main.event.RefreshSearchEvent
import com.wafflestudio.csereal.core.research.api.res.ResearchSearchResBody
import com.wafflestudio.csereal.core.research.database.*
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

interface ResearchSearchService {
    fun searchTopResearch(keyword: String, language: LanguageType, number: Int, amount: Int): ResearchSearchResBody
    fun searchResearch(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int,
        amount: Int
    ): ResearchSearchResBody
    fun deleteResearchSearch(researchSearchEntity: ResearchSearchEntity)
}

@Service
class ResearchSearchServiceImpl(
    private val labRepository: LabRepository,
    private val researchSearchRepository: ResearchSearchRepository,
    private val researchRepository: ResearchRepository,
    private val conferenceRepository: ConferenceRepository
) : ResearchSearchService {
    @Transactional(readOnly = true)
    override fun searchTopResearch(
        keyword: String,
        language: LanguageType,
        number: Int,
        amount: Int
    ): ResearchSearchResBody =
        researchSearchRepository.searchResearch(keyword, language, number, 1).let {
            ResearchSearchResBody.of(
                researches = it.first,
                keyword = keyword,
                amount = amount,
                total = it.second
            )
        }

    @Transactional(readOnly = true)
    override fun searchResearch(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int,
        amount: Int
    ): ResearchSearchResBody =
        researchSearchRepository.searchResearch(keyword, language, pageSize, pageNum).let {
            ResearchSearchResBody.of(
                researches = it.first,
                keyword = keyword,
                amount = amount,
                total = it.second
            )
        }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun refreshSearchListener(event: RefreshSearchEvent) {
        labRepository.findAll().forEach {
            it.researchSearch?.update(it) ?: let { _ ->
                it.researchSearch = ResearchSearchEntity.create(it)
            }
        }

        researchRepository.findAll().forEach {
            it.researchSearch?.update(it) ?: let { _ ->
                it.researchSearch = ResearchSearchEntity.create(it)
            }
        }

        conferenceRepository.findAll().forEach {
            it.researchSearch?.update(it) ?: let { _ ->
                it.researchSearch = ResearchSearchEntity.create(it)
            }
        }
    }

    @Transactional
    override fun deleteResearchSearch(
        researchSearchEntity: ResearchSearchEntity
    ) {
        researchSearchRepository.delete(researchSearchEntity)
    }
}
