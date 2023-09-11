package com.wafflestudio.csereal.core.member.database

import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.core.member.database.QMemberSearchEntity.memberSearchEntity
import com.wafflestudio.csereal.core.member.database.QProfessorEntity.professorEntity
import com.wafflestudio.csereal.core.member.database.QStaffEntity.staffEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface MemberSearchRepository
    : JpaRepository<MemberSearchEntity, Long>, MemberSearchRepositoryCustom {
}

interface MemberSearchRepositoryCustom {
    fun searchTopMember(keyword: String, number: Int): List<MemberSearchEntity>
    fun searchMember(keyword: String, pageSize: Int, pageNum: Int): Pair<List<MemberSearchEntity>, Long>
}

@Repository
class MemberSearchRepositoryCustomImpl (
        private val queryFactory: JPAQueryFactory,
): MemberSearchRepositoryCustom {

    override fun searchTopMember(keyword: String, number: Int): List<MemberSearchEntity> {
        return searchQuery(keyword)
                .limit(number.toLong())
                .fetch()
    }

    override fun searchMember(keyword: String, pageSize: Int, pageNum: Int): Pair<List<MemberSearchEntity>, Long> {
        val query = searchQuery(keyword)
        val total = getSearchCount(keyword)

        val validPageNum = exchangePageNum(pageSize, pageNum, total)
        val queryResult = query
                .offset((validPageNum-1) * pageSize.toLong())
                .limit(pageSize.toLong())
                .fetch()

        return queryResult to total
    }

    fun searchFullTextTemplate(keyword: String) =
        Expressions.numberTemplate(
                Double::class.javaObjectType,
                "function('match',{0},{1})",
                memberSearchEntity.content,
                keyword
        )

    fun searchQuery(keyword: String): JPAQuery<MemberSearchEntity> {
        val searchDoublTemplate = searchFullTextTemplate(keyword)

        return queryFactory.select(
                memberSearchEntity
        ).from(
                memberSearchEntity
        ).leftJoin(
                memberSearchEntity.professor, professorEntity
        ).fetchJoin()
                .leftJoin(
                        memberSearchEntity.staff, staffEntity
                ).fetchJoin()
                .where(
                        searchDoublTemplate.gt(0.0)
                )
    }

    fun getSearchCount(keyword: String): Long {
        val searchDoubleTemplate = searchFullTextTemplate(keyword)

        return queryFactory.select(
                memberSearchEntity
                    .countDistinct()
        ).from(
                memberSearchEntity
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