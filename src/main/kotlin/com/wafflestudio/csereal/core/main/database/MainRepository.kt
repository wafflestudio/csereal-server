package com.wafflestudio.csereal.core.main.database

import com.querydsl.core.QueryFactory
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sun.tools.javac.Main
import com.wafflestudio.csereal.core.main.dto.MainResponse
import com.wafflestudio.csereal.core.main.dto.NewsResponse
import com.wafflestudio.csereal.core.main.dto.NoticeResponse
import com.wafflestudio.csereal.core.news.database.QNewsEntity.newsEntity
import com.wafflestudio.csereal.core.notice.database.QNoticeEntity.noticeEntity
import com.wafflestudio.csereal.core.notice.database.QNoticeTagEntity.noticeTagEntity
import com.wafflestudio.csereal.core.notice.database.QTagInNoticeEntity.tagInNoticeEntity

import org.springframework.stereotype.Component

interface MainRepository : CustomMainRepository {
}

interface CustomMainRepository {
    fun readMain(): MainResponse
}

@Component
class MainRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CustomMainRepository {
    override fun readMain(): MainResponse {
        val slideList = queryFactory.select(
            Projections.constructor(
                NewsResponse::class.java,
                newsEntity.id,
                newsEntity.title,
                newsEntity.createdAt
            )
        ).from(newsEntity)
            .where(newsEntity.isDeleted.eq(false), newsEntity.isPublic.eq(true), newsEntity.isSlide.eq(true))
            .orderBy(newsEntity.isPinned.desc()).orderBy(newsEntity.createdAt.desc())
            .limit(10).fetch()

        val noticeTotalList = queryFactory.select(
            Projections.constructor(
                NoticeResponse::class.java,
                noticeEntity.id,
                noticeEntity.title,
                noticeEntity.createdAt
            )
        ).from(noticeEntity)
            .where(noticeEntity.isDeleted.eq(false), noticeEntity.isPublic.eq(true))
            .orderBy(noticeEntity.isPinned.desc()).orderBy(noticeEntity.createdAt.desc())
            .limit(5).fetch()

        val noticeAdmissionsList = makeNoticeEntityTagList("admissions")
        val noticeUndergraduateList = makeNoticeEntityTagList("undergraduate")
        val noticeGraduateList = makeNoticeEntityTagList("graduate")

        return MainResponse(slideList, noticeTotalList, noticeAdmissionsList, noticeUndergraduateList, noticeGraduateList)
    }

    private fun makeNoticeEntityTagList(tag: String) : List<NoticeResponse> {
        return queryFactory.select(
            Projections.constructor(
                NoticeResponse::class.java,
                noticeTagEntity.notice.id,
                noticeTagEntity.notice.title,
                noticeTagEntity.notice.createdAt,
            )
        ).from(noticeTagEntity)
            .rightJoin(noticeEntity).on(noticeTagEntity.notice.eq(noticeEntity))
            .rightJoin(tagInNoticeEntity).on(noticeTagEntity.tag.eq(tagInNoticeEntity))
            .where(noticeTagEntity.tag.name.eq(tag))
            .where(noticeEntity.isDeleted.eq(false), noticeEntity.isPublic.eq(true))
            .orderBy(noticeEntity.isPinned.desc()).orderBy(noticeEntity.createdAt.desc())
            .limit(5).distinct().fetch()

    }
}
