package com.wafflestudio.csereal.core.admissions.database

import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.common.repository.CommonRepository
import com.wafflestudio.csereal.common.utils.exchangePageNum
import com.wafflestudio.csereal.core.admissions.database.QAdmissionsEntity.admissionsEntity
import com.wafflestudio.csereal.core.admissions.type.AdmissionsMainType
import com.wafflestudio.csereal.core.admissions.type.AdmissionsPostType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface AdmissionsRepository : JpaRepository<AdmissionsEntity, Long>, AdmissionsCustomRepository {
    fun findByMainTypeAndPostTypeAndLanguage(
        mainType: AdmissionsMainType,
        postType: AdmissionsPostType,
        language: LanguageType
    ): AdmissionsEntity?
}

interface AdmissionsCustomRepository {
    fun searchAdmissions(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int
    ): Pair<List<AdmissionsEntity>, Long>
}

@Repository
class AdmissionsCustomRepositoryImpl(
    private val commonRepository: CommonRepository,
    private val queryFactory: JPAQueryFactory
) : AdmissionsCustomRepository {
    override fun searchAdmissions(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int
    ): Pair<List<AdmissionsEntity>, Long> {
        val total = searchCount(keyword, language)
        val validPageNum = exchangePageNum(pageSize, pageNum, total)
        val validOffset = (
            if (validPageNum >= 1) validPageNum - 1 else 0
            ) * pageSize.toLong()

        val result = searchQueryOfLanguage(keyword, language)
            .offset(validOffset)
            .limit(pageSize.toLong())
            .fetch()

        return result to total
    }
    fun searchCount(keyword: String, language: LanguageType) =
        searchQueryOfLanguage(keyword, language)
            .select(admissionsEntity.countDistinct())
            .fetchOne()!!

    fun searchQueryOfLanguage(keyword: String, language: LanguageType) =
        queryFactory.selectFrom(
            admissionsEntity
        ).where(
            commonRepository.searchFullSingleTextTemplate(
                keyword,
                admissionsEntity.searchContent
            ).gt(0.0),
            admissionsEntity.language.eq(language)
        )
}
