package com.wafflestudio.csereal.core.admissions.database

import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.common.repository.CommonRepository
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
    fun searchTopAdmissions(keyword: String, language: LanguageType, number: Int): List<AdmissionsEntity>
}

@Repository
class AdmissionsCustomRepositoryImpl(
    private val commonRepository: CommonRepository,
    private val queryFactory: JPAQueryFactory
) : AdmissionsCustomRepository {
    override fun searchTopAdmissions(
        keyword: String,
        language: LanguageType,
        number: Int
    ): List<AdmissionsEntity> =
        searchQueryOfLanguage(keyword, language)
            .limit(number.toLong())
            .fetch()

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
