package com.wafflestudio.csereal.core.notice.database

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.utils.FixedPageRequest
import com.wafflestudio.csereal.core.main.dto.MainImportantResponse
import com.wafflestudio.csereal.core.notice.database.QNoticeEntity.noticeEntity
import com.wafflestudio.csereal.core.notice.database.QNoticeTagEntity.noticeTagEntity
import com.wafflestudio.csereal.core.notice.dto.NoticeSearchDto
import com.wafflestudio.csereal.core.notice.dto.NoticeSearchResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.LocalDate

interface NoticeRepository : JpaRepository<NoticeEntity, Long>, CustomNoticeRepository {
    fun findFirstByIsPrivateFalseAndCreatedAtLessThanOrderByCreatedAtDesc(
        timestamp: LocalDateTime
    ): NoticeEntity?

    fun findFirstByIsPrivateFalseAndCreatedAtGreaterThanOrderByCreatedAtAsc(
        timestamp: LocalDateTime
    ): NoticeEntity?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        UPDATE notice n 
        SET n.isPinned = false, n.pinnedUntil = null 
        WHERE n.isPinned = true AND n.pinnedUntil < :currentDate
    """
    )
    fun updateExpiredPinnedStatus(@Param("currentDate") currentDate: LocalDate): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        UPDATE notice n 
        SET n.isImportant = false, n.importantUntil = null 
        WHERE n.isImportant = true AND n.importantUntil < :currentDate
    """
    )
    fun updateExpiredImportantStatus(@Param("currentDate") currentDate: LocalDate): Int
}

interface CustomNoticeRepository {
    /** 키워드 없이 태그만으로 훑을 때. 순서·페이징까지 여기서 정한다. */
    fun browseNotice(
        tag: List<String>?,
        pageable: Pageable,
        usePageBtn: Boolean,
        isStaff: Boolean
    ): NoticeSearchResponse

    /** 키워드 검색이 고른 id 들의 표시용 값. 준 id 차례대로 돌려준다. */
    fun findSearchDtosByIds(ids: List<Long>): List<NoticeSearchDto>

    fun findImportantNotice(cnt: Int? = null): List<MainImportantResponse>
}

@Component
class NoticeRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : CustomNoticeRepository {
    override fun findSearchDtosByIds(ids: List<Long>): List<NoticeSearchDto> {
        // in 질의는 순서를 보장하지 않는다. 부른 쪽이 정한 차례로 되돌린다.
        val byId = selectNoticeSearchDto().where(noticeEntity.id.`in`(ids)).fetch().associateBy { it.id }
        return ids.mapNotNull(byId::get)
    }

    override fun browseNotice(
        tag: List<String>?,
        pageable: Pageable,
        usePageBtn: Boolean,
        isStaff: Boolean
    ): NoticeSearchResponse {
        val tagsBooleanBuilder = BooleanBuilder()
        val isPrivateBooleanBuilder = BooleanBuilder()

        if (!tag.isNullOrEmpty()) {
            tag.forEach {
                val tagEnum = TagInNoticeEnum.getTagEnum(it)
                tagsBooleanBuilder.or(
                    noticeTagEntity.tag.name.eq(tagEnum)
                )
            }
        }

        if (!isStaff) {
            isPrivateBooleanBuilder.or(
                noticeEntity.isPrivate.eq(false)
            )
        }

        val jpaQuery = selectNoticeSearchDto()
            .leftJoin(noticeTagEntity).on(noticeTagEntity.notice.eq(noticeEntity))
            .where(tagsBooleanBuilder, isPrivateBooleanBuilder)

        val total: Long
        var pageRequest = pageable

        if (usePageBtn) {
            val countQuery = jpaQuery.clone()
            total = countQuery.select(noticeEntity.countDistinct()).fetchOne()!!
            pageRequest = FixedPageRequest(pageable, total)
        } else {
            total = (10 * pageable.pageSize).toLong() + 1 // 10개 페이지 고정
        }

        val noticeSearchDtoList = jpaQuery
            .offset(pageRequest.offset)
            .limit(pageRequest.pageSize.toLong())
            .distinct()
            .orderBy(noticeEntity.isPinned.desc())
            .orderBy(noticeEntity.createdAt.desc())
            .fetch()

        return NoticeSearchResponse(total, noticeSearchDtoList)
    }

    private fun selectNoticeSearchDto() = queryFactory.select(
        Projections.constructor(
            NoticeSearchDto::class.java,
            noticeEntity.id,
            noticeEntity.title,
            noticeEntity.createdAt,
            noticeEntity.isPinned,
            noticeEntity.attachments.isNotEmpty,
            noticeEntity.isPrivate
        )
    ).from(noticeEntity)

    override fun findImportantNotice(cnt: Int?): List<MainImportantResponse> =
        queryFactory.select(
            Projections.constructor(
                MainImportantResponse::class.java,
                noticeEntity.id,
                noticeEntity.titleForMain,
                noticeEntity.title,
                noticeEntity.plainTextDescription,
                noticeEntity.createdAt,
                Expressions.constant("notice")
            )
        ).from(noticeEntity)
            .where(
                noticeEntity.isImportant.isTrue(),
                noticeEntity.isPrivate.isFalse()
            ).orderBy(
                noticeEntity.createdAt.desc()
            ).let {
                if (cnt != null) {
                    it.limit(cnt.toLong())
                } else {
                    it
                }
            }
            .fetch()
}
