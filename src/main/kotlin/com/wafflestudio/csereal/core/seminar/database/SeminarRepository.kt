package com.wafflestudio.csereal.core.seminar.database

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.utils.FixedPageRequest
import com.wafflestudio.csereal.core.main.dto.MainImportantResponse
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import com.wafflestudio.csereal.core.seminar.database.QSeminarEntity.seminarEntity
import com.wafflestudio.csereal.core.seminar.dto.SeminarSearchDto
import com.wafflestudio.csereal.core.seminar.dto.SeminarSearchResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.LocalDate

interface SeminarRepository : JpaRepository<SeminarEntity, Long>, CustomSeminarRepository {
    fun findFirstByIsPrivateFalseAndCreatedAtLessThanOrderByCreatedAtDesc(
        timestamp: LocalDateTime
    ): SeminarEntity?

    fun findFirstByIsPrivateFalseAndCreatedAtGreaterThanOrderByCreatedAtAsc(
        timestamp: LocalDateTime
    ): SeminarEntity?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        "UPDATE seminar s SET s.isImportant = false, s.importantUntil = NULL " +
            "WHERE s.isImportant = true AND s.importantUntil < :currentDate"
    )
    fun updateExpiredImportantStatus(@Param("currentDate") currentDate: LocalDate): Int
}

interface CustomSeminarRepository {
    /** 키워드 없이 훑을 때. 순서·페이징까지 여기서 정한다. */
    fun browseSeminar(
        pageable: Pageable,
        usePageBtn: Boolean,
        isStaff: Boolean
    ): SeminarSearchResponse

    /** 키워드 검색이 고른 id 들의 표시용 값. 준 id 차례대로 돌려준다. */
    fun findSearchDtosByIds(ids: List<Long>): List<SeminarSearchDto>

    fun findImportantSeminar(cnt: Int? = null): List<MainImportantResponse>
}

@Component
class SeminarRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val mainImageService: MainImageService
) : CustomSeminarRepository {
    override fun findSearchDtosByIds(ids: List<Long>): List<SeminarSearchDto> {
        // in 질의는 순서를 보장하지 않는다. 부른 쪽이 정한 차례로 되돌린다.
        val byId = queryFactory.selectFrom(seminarEntity)
            .where(seminarEntity.id.`in`(ids))
            .fetch()
            .associateBy { it.id }
        return toSearchDtos(ids.mapNotNull(byId::get))
    }

    override fun browseSeminar(
        pageable: Pageable,
        usePageBtn: Boolean,
        isStaff: Boolean
    ): SeminarSearchResponse {
        val isPrivateBooleanBuilder = BooleanBuilder()

        if (!isStaff) {
            isPrivateBooleanBuilder.or(
                seminarEntity.isPrivate.eq(false)
            )
        }

        val jpaQuery = queryFactory.selectFrom(seminarEntity)
            .where(isPrivateBooleanBuilder)

        val total: Long
        var pageRequest = pageable

        if (usePageBtn) {
            val countQuery = jpaQuery.clone()
            total = countQuery.select(seminarEntity.count()).fetchOne()!!
            pageRequest = FixedPageRequest(pageable, total)
        } else {
            total = (10 * pageable.pageSize).toLong() + 1 // 10개 페이지 고정
        }

        val seminarEntityList = jpaQuery
            .offset(pageRequest.offset)
            .limit(pageRequest.pageSize.toLong())
            .orderBy(seminarEntity.startDate.desc())
            .fetch()

        return SeminarSearchResponse(total, toSearchDtos(seminarEntityList))
    }

    /** isYearLast 는 앞 항목과 견줘 정하므로 목록이 이미 화면 순서여야 한다. */
    private fun toSearchDtos(seminars: List<SeminarEntity>): List<SeminarSearchDto> =
        seminars.mapIndexed { index, seminar ->
            SeminarSearchDto(
                id = seminar.id,
                title = seminar.title,
                description = seminar.plainTextDescription,
                name = seminar.name,
                affiliation = seminar.affiliation,
                startDate = seminar.startDate,
                location = seminar.location,
                imageURL = mainImageService.createImageURL(seminar.mainImage),
                isYearLast = index == 0 || seminar.startDate.year != seminars[index - 1].startDate.year,
                isPrivate = seminar.isPrivate
            )
        }

    override fun findImportantSeminar(cnt: Int?): List<MainImportantResponse> =
        queryFactory.select(
            Projections.constructor(
                MainImportantResponse::class.java,
                seminarEntity.id,
                seminarEntity.titleForMain,
                seminarEntity.title,
                seminarEntity.plainTextDescription,
                seminarEntity.createdAt,
                Expressions.constant("seminar")
            )
        ).from(seminarEntity)
            .where(
                seminarEntity.isImportant.isTrue(),
                seminarEntity.isPrivate.isFalse()
            ).orderBy(
                seminarEntity.createdAt.desc()
            ).let {
                if (cnt != null) it.limit(cnt.toLong()) else it
            }
            .fetch()
}
