package com.wafflestudio.csereal.core.seminar.database

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.repository.CommonRepository
import com.wafflestudio.csereal.common.utils.FixedPageRequest
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import com.wafflestudio.csereal.core.seminar.database.QSeminarEntity.seminarEntity
import com.wafflestudio.csereal.core.seminar.dto.SeminarSearchDto
import com.wafflestudio.csereal.core.seminar.dto.SeminarSearchResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime

interface SeminarRepository : JpaRepository<SeminarEntity, Long>, CustomSeminarRepository {
    fun findAllByIsPrivateFalseAndIsImportantTrueAndIsDeletedFalse(): List<SeminarEntity>
    fun findAllByIsImportantTrueAndIsDeletedFalse(): List<SeminarEntity>
    fun findFirstByIsDeletedFalseAndIsPrivateFalseAndCreatedAtLessThanAndIsPrivateFalseOrderByCreatedAtDesc(timestamp: LocalDateTime): SeminarEntity?
    fun findFirstByIsDeletedFalseAndIsPrivateFalseAndCreatedAtGreaterThanAndIsPrivateFalseOrderByCreatedAtAsc(timestamp: LocalDateTime): SeminarEntity?
}

interface CustomSeminarRepository {
    fun searchSeminar(
        keyword: String?,
        pageable: Pageable,
        usePageBtn: Boolean,
        isStaff: Boolean
    ): SeminarSearchResponse
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

        val seminarEntityList = jpaQuery
            .orderBy(seminarEntity.createdAt.desc())
            .offset(pageRequest.offset)
            .limit(pageRequest.pageSize.toLong())
            .fetch()

        val seminarSearchDtoList: MutableList<SeminarSearchDto> = mutableListOf()

        for (i: Int in 0 until seminarEntityList.size) {
            var isYearLast = false
            if (i == seminarEntityList.size - 1) {
                isYearLast = true
            } else if (seminarEntityList[i].startDate?.year != seminarEntityList[i + 1].startDate?.year) {
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
}
