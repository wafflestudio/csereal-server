package com.wafflestudio.csereal.core.academics.database

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.common.repository.CommonRepository
import com.wafflestudio.csereal.common.utils.exchangeValidPageNum
import com.wafflestudio.csereal.core.academics.database.QAcademicsEntity.academicsEntity
import com.wafflestudio.csereal.core.academics.database.QAcademicsSearchEntity.academicsSearchEntity
import com.wafflestudio.csereal.core.academics.database.QCourseEntity.courseEntity
import com.wafflestudio.csereal.core.academics.database.QScholarshipEntity.scholarshipEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface AcademicsSearchRepository : JpaRepository<AcademicsSearchEntity, Long>, AcademicsSearchCustomRepository

interface AcademicsSearchCustomRepository {
    fun searchAcademics(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int
    ): Pair<List<AcademicsSearchEntity>, Long>
}

@Repository
class AcademicsSearchCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val commonRepository: CommonRepository
) : AcademicsSearchCustomRepository {
    override fun searchAcademics(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int
    ): Pair<List<AcademicsSearchEntity>, Long> {
        val query = searchQuery(keyword, language)
        val total = getSearchCount(keyword, language)

        val validPageNum = exchangeValidPageNum(pageSize, pageNum, total)
        val validOffset = (validPageNum - 1) * pageSize.toLong()

        val queryResult = query.offset(validOffset)
            .limit(pageSize.toLong())
            .fetch()

        return queryResult to total
    }

    fun searchQuery(keyword: String, language: LanguageType): JPAQuery<AcademicsSearchEntity> {
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
                searchDoubleTemplate.gt(0.0),
                academicsSearchEntity.language.eq(language)
            )
    }

    fun getSearchCount(keyword: String, language: LanguageType): Long {
        val searchDoubleTemplate = commonRepository.searchFullSingleTextTemplate(
            keyword,
            academicsSearchEntity.content
        )

        return queryFactory.select(
            academicsSearchEntity.countDistinct()
        ).from(academicsSearchEntity)
            .where(
                searchDoubleTemplate.gt(0.0),
                academicsSearchEntity.language.eq(language)
            ).fetchOne()!!
    }
}
