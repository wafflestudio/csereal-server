package com.wafflestudio.csereal.core.member.database

import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.repository.CommonRepository
import com.wafflestudio.csereal.common.utils.exchangeValidPageNum
import com.wafflestudio.csereal.core.member.database.QMemberSearchEntity.memberSearchEntity
import com.wafflestudio.csereal.core.member.database.QProfessorEntity.professorEntity
import com.wafflestudio.csereal.core.member.database.QStaffEntity.staffEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface MemberSearchRepository :
    JpaRepository<MemberSearchEntity, Long>, MemberSearchRepositoryCustom

interface MemberSearchRepositoryCustom {
    fun searchMember(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int
    ): Pair<List<MemberSearchEntity>, Long>
}

@Repository
class MemberSearchRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
    private val commonRepository: CommonRepository
) : MemberSearchRepositoryCustom {

    override fun searchMember(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int
    ): Pair<List<MemberSearchEntity>, Long> {
        val query = searchQuery(keyword, language)
        val total = getSearchCount(keyword, language)

        val validPageNum = exchangeValidPageNum(pageSize, pageNum, total)
        val queryResult = query
            .offset((validPageNum - 1) * pageSize.toLong())
            .limit(pageSize.toLong())
            .fetch()

        return queryResult to total
    }

    fun searchQuery(keyword: String, language: LanguageType): JPAQuery<MemberSearchEntity> {
        val searchDoubleTemplate = commonRepository.searchFullSingleTextTemplate(
            keyword,
            memberSearchEntity.content
        )

        return queryFactory.select(
            memberSearchEntity
        ).from(
            memberSearchEntity
        ).leftJoin(
            memberSearchEntity.professor,
            professorEntity
        ).fetchJoin()
            .leftJoin(
                memberSearchEntity.staff,
                staffEntity
            ).fetchJoin()
            .where(
                searchDoubleTemplate.gt(0.0),
                memberSearchEntity.language.eq(language)
            )
    }

    fun getSearchCount(keyword: String, language: LanguageType): Long {
        val searchDoubleTemplate = commonRepository.searchFullSingleTextTemplate(
            keyword,
            memberSearchEntity.content
        )

        return queryFactory.select(
            memberSearchEntity
                .countDistinct()
        ).from(
            memberSearchEntity
        ).where(
            searchDoubleTemplate.gt(0.0),
            memberSearchEntity.language.eq(language)
        ).fetchOne()!!
    }
}
