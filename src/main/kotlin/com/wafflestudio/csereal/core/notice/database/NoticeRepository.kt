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
    fun searchNotice(tags: List<Long>?, keyword: String?) : List<SearchResponse>
}
@Component
class NoticeRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CustomNoticeRepository {
    override fun searchNotice(tags: List<Long>?, keyword: String?): List<SearchResponse> {
        val booleanBuilder = BooleanBuilder()
        val booleanBuilder2 = BooleanBuilder()

        if (!keyword.isNullOrEmpty()) {
            booleanBuilder.and(
                noticeEntity.title.contains(keyword)
                    .or(noticeEntity.description.contains(keyword))
            )
        }
        if(!tags.isNullOrEmpty()) {
            tags.forEach {
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
            .rightJoin(noticeTagEntity.notice, noticeEntity)
            .where(noticeTagEntity.notice.eq(noticeEntity))
            .where(booleanBuilder2)
            .fetch()

    }

}