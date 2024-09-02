package com.wafflestudio.csereal.core.about.database

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.repository.CommonRepository
import com.wafflestudio.csereal.common.utils.exchangeValidPageNum
import com.wafflestudio.csereal.core.about.database.QAboutEntity.aboutEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface AboutRepository : JpaRepository<AboutEntity, Long>, AboutCustomRepository {
    fun findAllByLanguageAndPostTypeOrderByName(
        languageType: LanguageType,
        postType: AboutPostType
    ): List<AboutEntity>

    fun findByLanguageAndPostType(
        languageType: LanguageType,
        postType: AboutPostType
    ): AboutEntity
}

interface AboutCustomRepository {
    fun searchAbouts(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int
    ): Pair<List<AboutEntity>, Long>
}

@Repository
class AboutCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val commonRepository: CommonRepository
) : AboutCustomRepository {
    override fun searchAbouts(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int
    ): Pair<List<AboutEntity>, Long> {
        val total = searchCount(keyword, language)
        val validPageNum = exchangeValidPageNum(pageSize, pageNum, total)
        val validOffset = (validPageNum - 1) * pageSize.toLong()

        val queryResult = searchQueryExpr(keyword, language)
            .offset(validOffset)
            .limit(pageSize.toLong())
            .fetch()

        return queryResult to total
    }

    fun searchCount(keyword: String, language: LanguageType): Long {
        return searchQueryExpr(keyword, language)
            .select(aboutEntity.countDistinct())
            .fetchOne()!!
    }

    fun searchQueryExpr(keyword: String, language: LanguageType): JPAQuery<AboutEntity> {
        val matchExpression = commonRepository.searchFullSingleTextTemplate(
            keyword,
            aboutEntity.searchContent
        )

        return queryFactory.select(aboutEntity)
            .from(aboutEntity)
            .where(
                matchExpression.gt(0.0),
                aboutEntity.language.eq(language)
            )
    }
}
