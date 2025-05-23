package com.wafflestudio.csereal.core.seminar.database

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.enums.ContentSearchSortType
import com.wafflestudio.csereal.common.repository.CommonRepository
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
    fun findFirstByIsDeletedFalseAndIsPrivateFalseAndCreatedAtLessThanOrderByCreatedAtDesc(
        timestamp: LocalDateTime
    ): SeminarEntity?

    fun findFirstByIsDeletedFalseAndIsPrivateFalseAndCreatedAtGreaterThanOrderByCreatedAtAsc(
        timestamp: LocalDateTime
    ): SeminarEntity?

    @Query("SELECT s.id FROM seminar s")
    fun findAllIds(): List<Long>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        "UPDATE seminar s SET s.isImportant = false, s.importantUntil = NULL " +
            "WHERE s.isImportant = true AND s.importantUntil < :currentDate"
    )
    fun updateExpiredImportantStatus(@Param("currentDate") currentDate: LocalDate): Int
}

interface CustomSeminarRepository {
    fun searchSeminar(
        keyword: String?,
        pageable: Pageable,
        usePageBtn: Boolean,
        sortBy: ContentSearchSortType,
        isStaff: Boolean
    ): SeminarSearchResponse

    fun findImportantSeminar(cnt: Int? = null): List<MainImportantResponse>
}

@Component
class SeminarRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val mainImageService: MainImageService,
    private val commonRepository: CommonRepository
) : CustomSeminarRepository {
    override fun searchSeminar(
        keyword: String?,
        pageable: Pageable,
        usePageBtn: Boolean,
        sortBy: ContentSearchSortType,
        isStaff: Boolean
    ): SeminarSearchResponse {
        val keywordBooleanBuilder = BooleanBuilder()
        val isPrivateBooleanBuilder = BooleanBuilder()

        if (!keyword.isNullOrEmpty()) {
            val booleanTemplate = commonRepository.searchFullSeptupleTextTemplate(
                keyword,
                seminarEntity.title,
                seminarEntity.name,
                seminarEntity.affiliation,
                seminarEntity.location,
                seminarEntity.plainTextDescription,
                seminarEntity.plainTextIntroduction,
                seminarEntity.plainTextAdditionalNote
            )
            keywordBooleanBuilder.and(booleanTemplate.gt(0.0))
        }

        if (!isStaff) {
            isPrivateBooleanBuilder.or(
                seminarEntity.isPrivate.eq(false)
            )
        }

        val jpaQuery = queryFactory.selectFrom(seminarEntity)
            .where(seminarEntity.isDeleted.eq(false))
            .where(keywordBooleanBuilder, isPrivateBooleanBuilder)

        val total: Long
        var pageRequest = pageable

        if (usePageBtn) {
            val countQuery = jpaQuery.clone()
            total = countQuery.select(seminarEntity.count()).fetchOne()!!
            pageRequest = FixedPageRequest(pageable, total)
        } else {
            total = (10 * pageable.pageSize).toLong() + 1 // 10개 페이지 고정
        }

        val seminarEntityQuery = jpaQuery
            .offset(pageRequest.offset)
            .limit(pageRequest.pageSize.toLong())

        val seminarEntityList = when {
            sortBy == ContentSearchSortType.DATE || keyword.isNullOrEmpty() ->
                seminarEntityQuery.orderBy(
                    seminarEntity.startDate.desc()
                )

            else /* sortBy == RELEVANCE */ -> seminarEntityQuery
        }.fetch()

        val seminarSearchDtoList: MutableList<SeminarSearchDto> = mutableListOf()

        for (i: Int in 0 until seminarEntityList.size) {
            var isYearLast = false
            if (i == 0) {
                isYearLast = true
            } else if (seminarEntityList[i].startDate?.year != seminarEntityList[i - 1].startDate?.year) {
                isYearLast = true
            }

            val imageURL = mainImageService.createImageURL(seminarEntityList[i].mainImage)

            seminarSearchDtoList.add(
                SeminarSearchDto(
                    id = seminarEntityList[i].id,
                    title = seminarEntityList[i].title,
                    description = seminarEntityList[i].plainTextDescription,
                    name = seminarEntityList[i].name,
                    affiliation = seminarEntityList[i].affiliation,
                    startDate = seminarEntityList[i].startDate,
                    location = seminarEntityList[i].location,
                    imageURL = imageURL,
                    isYearLast = isYearLast,
                    isPrivate = seminarEntityList[i].isPrivate
                )
            )
        }

        return SeminarSearchResponse(total, seminarSearchDtoList)
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
                seminarEntity.isDeleted.isFalse(),
                seminarEntity.isPrivate.isFalse()
            ).orderBy(
                seminarEntity.createdAt.desc()
            ).let {
                if (cnt != null) it.limit(cnt.toLong()) else it
            }
            .fetch()
}
