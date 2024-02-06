package com.wafflestudio.csereal.core.academics.database

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.repository.CommonRepository
import com.wafflestudio.csereal.core.academics.database.QAcademicsEntity.academicsEntity
import com.wafflestudio.csereal.core.academics.database.QAcademicsSearchEntity.academicsSearchEntity
import com.wafflestudio.csereal.core.academics.database.QCourseEntity.courseEntity
import com.wafflestudio.csereal.core.academics.database.QScholarshipEntity.scholarshipEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import com.wafflestudio.csereal.common.utils.exchangePageNum

interface AcademicsSearchRepository : JpaRepository<AcademicsSearchEntity, Long>, AcademicsSearchCustomRepository

interface AcademicsSearchCustomRepository {
    fun searchAcademics(keyword: String, pageSize: Int, pageNum: Int): Pair<List<AcademicsSearchEntity>, Long>
    fun searchTopAcademics(keyword: String, number: Int): List<AcademicsSearchEntity>
}

@Repository
class AcademicsSearchCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val commonRepository: CommonRepository
) : AcademicsSearchCustomRepository {
    override fun searchTopAcademics(keyword: String, number: Int): List<AcademicsSearchEntity> {
        return searchQuery(keyword)
            .limit(number.toLong())
            .fetch()
    }

    override fun searchAcademics(
        keyword: String,
        pageSize: Int,
        pageNum: Int
    ): Pair<List<AcademicsSearchEntity>, Long> {
        val query = searchQuery(keyword)
        val total = getSearchCount(keyword)

        val validPageNum = exchangePageNum(pageSize, pageNum, total)
        val validOffset = (if (validPageNum >= 1) validPageNum - 1 else 0) * pageSize.toLong()
        val queryResult = query.offset(validOffset)
            .limit(pageSize.toLong())
            .fetch()

        return queryResult to total
    }

    fun searchQuery(keyword: String): JPAQuery<AcademicsSearchEntity> {
        val searchDoubleTemplate = commonRepository.searchFullSingleTextTemplate(
            keyword,
            academicsSearchEntity.content
        )

        return queryFactory.selectFrom(
            academicsSearchEntity
        ).leftJoin(
            academicsSearchEntity.academics,
            academicsEntity
        ).fetchJoin()
            .leftJoin(
                academicsSearchEntity.course,
                courseEntity
            ).fetchJoin()
            .leftJoin(
                academicsSearchEntity.scholarship,
                scholarshipEntity
            ).fetchJoin()
            .where(
                searchDoubleTemplate.gt(0.0)
            )
    }

    fun getSearchCount(keyword: String): Long {
        val searchDoubleTemplate = commonRepository.searchFullSingleTextTemplate(
            keyword,
            academicsSearchEntity.content
        )

        return queryFactory.select(
            academicsSearchEntity.countDistinct()
        ).from(academicsSearchEntity)
            .where(
                searchDoubleTemplate.gt(0.0)
            ).fetchOne()!!
    }
}
