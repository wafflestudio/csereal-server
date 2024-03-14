package com.wafflestudio.csereal.core.notice.database

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.repository.CommonRepository
import com.wafflestudio.csereal.common.utils.FixedPageRequest
import com.wafflestudio.csereal.core.notice.database.QNoticeEntity.noticeEntity
import com.wafflestudio.csereal.core.notice.database.QNoticeTagEntity.noticeTagEntity
import com.wafflestudio.csereal.core.notice.dto.NoticeSearchDto
import com.wafflestudio.csereal.core.notice.dto.NoticeSearchResponse
import com.wafflestudio.csereal.core.notice.dto.NoticeTotalSearchElement
import com.wafflestudio.csereal.core.notice.dto.NoticeTotalSearchResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime

interface NoticeRepository : JpaRepository<NoticeEntity, Long>, CustomNoticeRepository {
    fun findByIdAndIsPrivateFalse(id: Long): NoticeEntity?
    fun findAllByIsPrivateFalseAndIsImportantTrueAndIsDeletedFalse(): List<NoticeEntity>
    fun findAllByIsImportantTrueAndIsDeletedFalse(): List<NoticeEntity>
    fun findFirstByIsDeletedFalseAndIsPrivateFalseAndCreatedAtLessThanOrderByCreatedAtDesc(
        timestamp: LocalDateTime
    ): NoticeEntity?

    fun findFirstByIsDeletedFalseAndIsPrivateFalseAndCreatedAtGreaterThanOrderByCreatedAtAsc(
        timestamp: LocalDateTime
    ): NoticeEntity?
}

interface CustomNoticeRepository {
    fun searchNotice(
        tag: List<String>?,
        keyword: String?,
        pageable: Pageable,
        usePageBtn: Boolean,
        isStaff: Boolean
    ): NoticeSearchResponse

    fun totalSearchNotice(keyword: String, number: Int, stringLength: Int): NoticeTotalSearchResponse
}

@Component
class NoticeRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val commonRepository: CommonRepository
) : CustomNoticeRepository {
    override fun totalSearchNotice(
        keyword: String,
        number: Int,
        stringLength: Int
    ): NoticeTotalSearchResponse {
        val doubleTemplate = commonRepository.searchFullDoubleTextTemplate(
            keyword,
            noticeEntity.title,
            noticeEntity.plainTextDescription
        )

        val query = queryFactory.select(
            noticeEntity.id,
            noticeEntity.title,
            noticeEntity.createdAt,
            noticeEntity.plainTextDescription
        ).from(noticeEntity)
            .where(doubleTemplate.gt(0.0))

        val total = query.clone().select(noticeEntity.countDistinct()).fetchOne()!!

        val searchResult = query
            .orderBy(noticeEntity.createdAt.desc())
            .limit(number.toLong())
            .fetch()

        return NoticeTotalSearchResponse(
            total.toInt(),
            searchResult.map {
                NoticeTotalSearchElement(
                    it[noticeEntity.id]!!,
                    it[noticeEntity.title]!!,
                    it[noticeEntity.createdAt]!!,
                    it[noticeEntity.plainTextDescription]!!,
                    keyword,
                    stringLength
                )
            }
        )
    }

    override fun searchNotice(
        tag: List<String>?,
        keyword: String?,
        pageable: Pageable,
        usePageBtn: Boolean,
        isStaff: Boolean
    ): NoticeSearchResponse {
        val keywordBooleanBuilder = BooleanBuilder()
        val tagsBooleanBuilder = BooleanBuilder()
        val isPrivateBooleanBuilder = BooleanBuilder()

        if (!keyword.isNullOrEmpty()) {
            val booleanTemplate = commonRepository.searchFullDoubleTextTemplate(
                keyword,
                noticeEntity.title,
                noticeEntity.plainTextDescription
            )
            keywordBooleanBuilder.and(booleanTemplate.gt(0.0))
        }

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

        val jpaQuery = queryFactory.select(
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
            .leftJoin(noticeTagEntity).on(noticeTagEntity.notice.eq(noticeEntity))
            .where(noticeEntity.isDeleted.eq(false))
            .where(keywordBooleanBuilder, tagsBooleanBuilder, isPrivateBooleanBuilder)

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
            .orderBy(noticeEntity.isPinned.desc())
            .orderBy(noticeEntity.createdAt.desc())
            .offset(pageRequest.offset)
            .limit(pageRequest.pageSize.toLong())
            .distinct()
            .fetch()

        return NoticeSearchResponse(total, noticeSearchDtoList)
    }
}
