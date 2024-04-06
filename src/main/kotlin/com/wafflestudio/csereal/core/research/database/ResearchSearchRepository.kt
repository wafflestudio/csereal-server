package com.wafflestudio.csereal.core.research.database

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.repository.CommonRepository
import com.wafflestudio.csereal.common.utils.exchangeValidPageNum
import com.wafflestudio.csereal.core.conference.database.QConferenceEntity.conferenceEntity
import com.wafflestudio.csereal.core.research.database.QLabEntity.labEntity
import com.wafflestudio.csereal.core.research.database.QResearchEntity.researchEntity
import com.wafflestudio.csereal.core.research.database.QResearchSearchEntity.researchSearchEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface ResearchSearchRepository : JpaRepository<ResearchSearchEntity, Long>, ResearchSearchRepositoryCustom

interface ResearchSearchRepositoryCustom {
    fun searchResearch(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int
    ): Pair<List<ResearchSearchEntity>, Long>
}

@Repository
class ResearchSearchRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
    private val commonRepository: CommonRepository
) : ResearchSearchRepositoryCustom {
    override fun searchResearch(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int
    ): Pair<List<ResearchSearchEntity>, Long> {
        val query = searchQuery(keyword, language)
        val total = getSearchCount(keyword, language)

        val validPageNum = exchangeValidPageNum(pageSize, pageNum, total)
        val validOffset = (validPageNum - 1) * pageSize.toLong()
        val queryResult = query
            .offset(validOffset)
            .limit(pageSize.toLong())
            .fetch()

        return queryResult to total
    }

    fun searchQuery(keyword: String, language: LanguageType): JPAQuery<ResearchSearchEntity> {
        val searchDoubleTemplate = commonRepository.searchFullSingleTextTemplate(
            keyword,
            researchSearchEntity.content
        )

        return queryFactory.selectFrom(
            researchSearchEntity
        ).leftJoin(
            researchSearchEntity.lab,
            labEntity
        ).fetchJoin()
            .leftJoin(
                researchSearchEntity.research,
                researchEntity
            ).fetchJoin()
            .leftJoin(
                researchSearchEntity.conferenceElement,
                conferenceEntity
            ).fetchJoin()
            .where(
                searchDoubleTemplate.gt(0.0),
                researchSearchEntity.language.eq(language)
            )
    }

    fun getSearchCount(keyword: String, language: LanguageType): Long {
        val searchDoubleTemplate = commonRepository.searchFullSingleTextTemplate(
            keyword,
            researchSearchEntity.content
        )

        return queryFactory.select(
            researchSearchEntity
                .countDistinct()
        ).from(
            researchSearchEntity
        ).where(
            searchDoubleTemplate.gt(0.0),
            researchSearchEntity.language.eq(language)
        ).fetchOne()!!
    }
}
