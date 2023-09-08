package com.wafflestudio.csereal.core.member.database

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface MemberSearchRepository
    : JpaRepository<MemberSearchEntity, Long>, MemberSearchRepositoryCustom {
}

interface MemberSearchRepositoryCustom {

}

@Repository
class MemberSearchRepositoryCustomImpl (
        private val queryFactory: JPAQueryFactory,
): MemberSearchRepositoryCustom {
}