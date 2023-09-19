package com.wafflestudio.csereal.core.research.database

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface ResearchSearchRepository : JpaRepository<ResearchSearchEntity, Long>, ResearchSearchRepositoryCustom

interface ResearchSearchRepositoryCustom

@Repository
class ResearchSearchRepositoryCustomImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : ResearchSearchRepositoryCustom
