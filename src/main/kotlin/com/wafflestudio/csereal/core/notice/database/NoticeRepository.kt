package com.wafflestudio.csereal.core.notice.database

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.core.notice.database.QNoticeEntity.noticeEntity
import com.wafflestudio.csereal.core.notice.database.QNoticeTagEntity.noticeTagEntity
import com.wafflestudio.csereal.core.notice.dto.NoticeDto
import com.wafflestudio.csereal.core.notice.dto.SearchResponse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

interface NoticeRepository : JpaRepository<NoticeEntity, Long>, CustomNoticeRepository {
}

interface CustomNoticeRepository {
    fun searchNotice(tag: List<Long>?, keyword: String?, pageNum: Long): List<SearchResponse>
}
@Component
class NoticeRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CustomNoticeRepository {
    override fun searchNotice(tag: List<Long>?, keyword: String?, pageNum: Long): List<SearchResponse> {
        val booleanBuilder = BooleanBuilder()
        val booleanBuilder2 = BooleanBuilder()

        if (!keyword.isNullOrEmpty()) {
            booleanBuilder.and(
                noticeEntity.title.contains(keyword)
                    .or(noticeEntity.description.contains(keyword))
            )
        }
        if(!tag.isNullOrEmpty()) {
            tag.forEach {
                booleanBuilder2.or(
                    noticeTagEntity.tag.id.eq(it)
                )
            }
        }


        return queryFactory.select(
            Projections.constructor(
                SearchResponse::class.java,
                noticeEntity.id,
                noticeEntity.title,
                noticeEntity.createdAt,
                noticeTagEntity.tag.id
            )
        ).from(noticeTagEntity)
            .rightJoin(noticeTagEntity.notice, noticeEntity).on(noticeEntity.isDeleted.eq(false), noticeEntity.isPublic.eq(true))
            .where(noticeTagEntity.notice.eq(noticeEntity))
            .where(booleanBuilder2)
            .orderBy(noticeEntity.isPinned.desc())
            .orderBy(noticeEntity.createdAt.desc())
            .offset(5*pageNum)  //로컬 테스트를 위해 잠시 5로 둘 것, 원래는 20
            .limit(5)
            .fetch()

    }

}