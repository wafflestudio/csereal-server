package com.wafflestudio.csereal.core.seminar.database

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.utils.FixedPageRequest
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import com.wafflestudio.csereal.core.seminar.database.QSeminarEntity.seminarEntity
import com.wafflestudio.csereal.core.seminar.dto.SeminarSearchDto
import com.wafflestudio.csereal.core.seminar.dto.SeminarSearchResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime

interface SeminarRepository : JpaRepository<SeminarEntity, Long>, CustomSeminarRepository {
    fun findAllByIsImportant(isImportant: Boolean): List<SeminarEntity>
    fun findFirstByCreatedAtLessThanOrderByCreatedAtDesc(timestamp: LocalDateTime): SeminarEntity?
    fun findFirstByCreatedAtGreaterThanOrderByCreatedAtAsc(timestamp: LocalDateTime): SeminarEntity?
}

interface CustomSeminarRepository {
    fun searchSeminar(keyword: String?, pageable: Pageable, usePageBtn: Boolean): SeminarSearchResponse
}

@Component
class SeminarRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val mainImageService: MainImageService,
) : CustomSeminarRepository {
    override fun searchSeminar(keyword: String?, pageable: Pageable, usePageBtn: Boolean): SeminarSearchResponse {
        val keywordBooleanBuilder = BooleanBuilder()

        if (!keyword.isNullOrEmpty()) {
            val keywordList = keyword.split("[^a-zA-Z0-9가-힣]".toRegex())
            keywordList.forEach {
                if (it.length == 1) {
                    throw CserealException.Csereal400("각각의 키워드는 한글자 이상이어야 합니다.")
                } else {
                    keywordBooleanBuilder.and(
                        seminarEntity.title.contains(it)
                            .or(seminarEntity.name.contains(it))
                            .or(seminarEntity.affiliation.contains(it))
                            .or(seminarEntity.location.contains(it))
                    )
                }
            }
        }

        val jpaQuery = queryFactory.selectFrom(seminarEntity)
            .where(seminarEntity.isDeleted.eq(false), seminarEntity.isPublic.eq(true))
            .where(keywordBooleanBuilder)

        val total: Long
        var pageRequest = pageable

        if (usePageBtn) {
            val countQuery = jpaQuery.clone()
            total = countQuery.select(seminarEntity.count()).fetchOne()!!
            pageRequest = FixedPageRequest(pageable, total)
        } else {
            total = (10 * pageable.pageSize).toLong() // 10개 페이지 고정
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
            } else if (seminarEntityList[i].startDate?.substring(0, 4) != seminarEntityList[i + 1].startDate?.substring(
                    0,
                    4
                )
            ) {
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
                )
            )
        }

        return SeminarSearchResponse(total, seminarSearchDtoList)
    }
}
