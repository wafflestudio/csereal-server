package com.wafflestudio.csereal.core.about.database

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.repository.CommonRepository
import com.wafflestudio.csereal.common.utils.exchangeValidPageNum
import com.wafflestudio.csereal.core.about.database.QAboutTranslationEntity.aboutTranslationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface AboutRepository : JpaRepository<AboutEntity, Long> {
    fun findAllByPostType(postType: AboutPostType): List<AboutEntity>

    // 싱글턴(개요·인사말·연혁·졸업생 진로·연락처)만 쓴다. 여러 행인 종류에 부르면
    // 결과가 하나가 아니라 Spring Data 가 예외를 던진다.
    fun findByPostType(postType: AboutPostType): AboutEntity
}

interface AboutTranslationRepository :
    JpaRepository<AboutTranslationEntity, Long>,
    AboutTranslationCustomRepository

interface AboutTranslationCustomRepository {
    fun searchAbouts(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int
    ): Pair<List<AboutTranslationEntity>, Long>
}

@Repository
class AboutTranslationCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val commonRepository: CommonRepository
) : AboutTranslationCustomRepository {
    override fun searchAbouts(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int
    ): Pair<List<AboutTranslationEntity>, Long> {
        val total = searchCount(keyword, language)
        val validPageNum = exchangeValidPageNum(pageSize, pageNum, total)
        val validOffset = (validPageNum - 1) * pageSize.toLong()

        val queryResult = searchQueryExpr(keyword, language)
            .offset(validOffset)
            .limit(pageSize.toLong())
            .fetch()

        return queryResult to total
    }

    fun searchCount(keyword: String, language: LanguageType): Long =
        searchQueryExpr(keyword, language)
            .select(aboutTranslationEntity.countDistinct())
            .fetchOne()!!

    // 색인이 번역본에 있으므로 검색도 번역본을 훑는다.
    fun searchQueryExpr(keyword: String, language: LanguageType): JPAQuery<AboutTranslationEntity> {
        val matchExpression = commonRepository.searchFullSingleTextTemplate(
            keyword,
            aboutTranslationEntity.searchContent
        )

        return queryFactory.select(aboutTranslationEntity)
            .from(aboutTranslationEntity)
            .where(
                matchExpression.gt(0.0),
                aboutTranslationEntity.language.eq(language)
            )
    }
}
