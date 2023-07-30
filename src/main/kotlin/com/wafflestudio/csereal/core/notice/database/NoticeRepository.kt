package com.wafflestudio.csereal.core.notice.database

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.CserealException
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
        val keywordBooleanBuilder = BooleanBuilder()
        val tagsBooleanBuilder = BooleanBuilder()

        if (!keyword.isNullOrEmpty()) {

            val keywordList = keyword.split("[^a-zA-Z0-9가-힣]".toRegex())
            keywordList.forEach {
                if(it.length == 1) {
                    throw CserealException.Csereal400("각각의 키워드는 한글자 이상이어야 합니다.")
                } else {
                    keywordBooleanBuilder.and(
                        noticeEntity.title.contains(it)
                            .or(noticeEntity.description.contains(it))
                    )
                }

            }

        }
        if(!tag.isNullOrEmpty()) {
            tag.forEach {
                tagsBooleanBuilder.or(
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
            )
        ).from(noticeEntity)
            .innerJoin(noticeTagEntity).on(noticeTagEntity.notice.eq(noticeEntity))
            .where(noticeEntity.isDeleted.eq(false), noticeEntity.isPublic.eq(true))
            .where(keywordBooleanBuilder)
            .where(tagsBooleanBuilder)
            .orderBy(noticeEntity.isPinned.desc())
            .orderBy(noticeEntity.createdAt.desc())
            .offset(20*pageNum)
            .limit(20)
            .distinct()
            .fetch()

    }

}