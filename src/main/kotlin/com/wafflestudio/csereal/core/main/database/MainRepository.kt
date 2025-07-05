package com.wafflestudio.csereal.core.main.database

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.core.main.dto.MainNoticeResponse
import com.wafflestudio.csereal.core.main.dto.MainSlideResponse
import com.wafflestudio.csereal.core.news.database.QNewsEntity.newsEntity
import com.wafflestudio.csereal.core.notice.database.QNoticeEntity.noticeEntity
import com.wafflestudio.csereal.core.notice.database.QNoticeTagEntity.noticeTagEntity
import com.wafflestudio.csereal.core.notice.database.QTagInNoticeEntity.tagInNoticeEntity
import com.wafflestudio.csereal.core.notice.database.TagInNoticeEnum
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService

import org.springframework.stereotype.Component

// TODO: Refactor to each repository (and service)
interface MainRepository {
    fun readMainSlide(): List<MainSlideResponse>
    fun readMainNoticeTotal(): List<MainNoticeResponse>
    fun readMainNoticeTag(tagEnum: TagInNoticeEnum): List<MainNoticeResponse>
}

@Component
class MainRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val mainImageService: MainImageService
) : MainRepository {
    override fun readMainSlide(): List<MainSlideResponse> {
        val newsEntityList = queryFactory.selectFrom(newsEntity)
            .where(newsEntity.isPrivate.eq(false), newsEntity.isSlide.eq(true))
            .orderBy(newsEntity.createdAt.desc())
            .limit(20).fetch()

        return newsEntityList.map {
            val imageURL = mainImageService.createImageURL(it.mainImage)
            MainSlideResponse(
                id = it.id,
                title = it.title,
                imageURL = imageURL,
                createdAt = it.createdAt,
                description = it.plainTextDescription.substring(
                    0,
                    100.coerceAtMost(it.plainTextDescription.length)
                )
            )
        }
    }

    override fun readMainNoticeTotal(): List<MainNoticeResponse> {
        return queryFactory.select(
            Projections.constructor(
                MainNoticeResponse::class.java,
                noticeEntity.id,
                noticeEntity.title,
                noticeEntity.createdAt,
                noticeEntity.isPinned
            )
        ).from(noticeEntity)
            .where(noticeEntity.isPrivate.eq(false))
            .orderBy(noticeEntity.createdAt.desc())
            .limit(6).fetch()
    }

    override fun readMainNoticeTag(tagEnum: TagInNoticeEnum): List<MainNoticeResponse> {
        return queryFactory.select(
            Projections.constructor(
                MainNoticeResponse::class.java,
                noticeTagEntity.notice.id,
                noticeTagEntity.notice.title,
                noticeTagEntity.notice.createdAt,
                noticeEntity.isPinned
            )
        ).from(noticeTagEntity)
            .rightJoin(noticeEntity).on(noticeTagEntity.notice.eq(noticeEntity))
            .rightJoin(tagInNoticeEntity).on(noticeTagEntity.tag.eq(tagInNoticeEntity))
            .where(noticeTagEntity.tag.name.eq(tagEnum))
            .where(noticeEntity.isPrivate.eq(false))
            .orderBy(noticeTagEntity.notice.createdAt.desc())
            .limit(6).distinct().fetch()
    }
}
