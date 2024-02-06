package com.wafflestudio.csereal.core.research.database

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.repository.CommonRepository
import com.wafflestudio.csereal.core.conference.database.QConferenceEntity.conferenceEntity
import com.wafflestudio.csereal.core.research.database.QLabEntity.labEntity
import com.wafflestudio.csereal.core.research.database.QResearchEntity.researchEntity
import com.wafflestudio.csereal.core.research.database.QResearchSearchEntity.researchSearchEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface ResearchSearchRepository : JpaRepository<ResearchSearchEntity, Long>, ResearchSearchRepositoryCustom

interface ResearchSearchRepositoryCustom {
    fun searchTopResearch(keyword: String, number: Int): List<ResearchSearchEntity>

    fun searchResearch(keyword: String, pageSize: Int, pageNum: Int): Pair<List<ResearchSearchEntity>, Long>
}

@Repository
class ResearchSearchRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
    private val commonRepository: CommonRepository
) : ResearchSearchRepositoryCustom {
    override fun searchTopResearch(keyword: String, number: Int): List<ResearchSearchEntity> {
        return searchQuery(keyword)
            .limit(number.toLong())
            .fetch()
    }

    override fun searchResearch(keyword: String, pageSize: Int, pageNum: Int): Pair<List<ResearchSearchEntity>, Long> {
        val query = searchQuery(keyword)
        val total = getSearchCount(keyword)

        val validPageNum = exchangePageNum(pageSize, pageNum, total)
        val validOffset = (if (validPageNum >= 1) validPageNum - 1 else 0) * pageSize.toLong()
        val queryResult = query
            .offset(validOffset)
            .limit(pageSize.toLong())
            .fetch()

        return queryResult to total
    }

    fun searchQuery(keyword: String): JPAQuery<ResearchSearchEntity> {
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
                searchDoubleTemplate.gt(0.0)
            )
    }

    fun getSearchCount(keyword: String): Long {
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
            searchDoubleTemplate.gt(0.0)
        ).fetchOne()!!
    }

    fun exchangePageNum(pageSize: Int, pageNum: Int, total: Long): Int {
        return if ((pageNum - 1) * pageSize < total) {
            pageNum
        } else {
            Math.ceil(total.toDouble() / pageSize).toInt()
        }
    }
}
