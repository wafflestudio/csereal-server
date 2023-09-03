package com.wafflestudio.csereal.core.admin.database

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.core.admin.dto.SlideResponse
import com.wafflestudio.csereal.core.news.database.QNewsEntity.newsEntity
import org.springframework.stereotype.Component

interface AdminRepository {
    fun readAllSlides(pageNum: Long): List<SlideResponse>
}

@Component
class AdminRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
): AdminRepository {
    override fun readAllSlides(pageNum: Long): List<SlideResponse> {
        return queryFactory.select(
            Projections.constructor(
                SlideResponse::class.java,
                newsEntity.id,
                newsEntity.title,
                newsEntity.createdAt
            )
        ).from(newsEntity)
            .where(newsEntity.isDeleted.eq(false), newsEntity.isPublic.eq(true), newsEntity.isSlide.eq(true))
            .orderBy(newsEntity.createdAt.desc())
            .offset(40*pageNum)
            .limit(40)
            .fetch()
    }
}