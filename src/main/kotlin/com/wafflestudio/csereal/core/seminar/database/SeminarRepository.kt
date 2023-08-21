package com.wafflestudio.csereal.core.seminar.database

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.seminar.database.QSeminarEntity.seminarEntity
import com.wafflestudio.csereal.core.seminar.dto.SeminarSearchDto
import com.wafflestudio.csereal.core.seminar.dto.SeminarSearchResponse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

interface SeminarRepository : JpaRepository<SeminarEntity, Long>, CustomSeminarRepository {
}

interface CustomSeminarRepository {
    fun searchSeminar(keyword: String?, pageNum: Long): SeminarSearchResponse
    fun findPrevNextId(seminarId: Long, keyword: String?): Array<SeminarEntity?>?
}

@Component
class SeminarRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CustomSeminarRepository {
    override fun searchSeminar(keyword: String?, pageNum: Long): SeminarSearchResponse {
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

        val jpaQuery = queryFactory.select(seminarEntity).from(seminarEntity)
            .where(seminarEntity.isDeleted.eq(false))
            .where(keywordBooleanBuilder)

        val total = jpaQuery.fetch().size

        val seminarEntityList = jpaQuery.orderBy(seminarEntity.createdAt.desc())
            .offset(10*pageNum)
            .limit(20)
            .fetch()

        val seminarSearchDtoList : MutableList<SeminarSearchDto> = mutableListOf()

        for(i: Int in 0 until seminarEntityList.size) {
            var isYearLast = false
            if(i == seminarEntityList.size-1) {
                isYearLast = true
            } else if(seminarEntityList[i].startDate?.substring(0,4) != seminarEntityList[i+1].startDate?.substring(0,4)) {
                isYearLast = true
            }

            seminarSearchDtoList.add(
                SeminarSearchDto(
                    id = seminarEntityList[i].id,
                    title = seminarEntityList[i].title,
                    startDate = seminarEntityList[i].startDate,
                    isYearLast = isYearLast,
                    name = seminarEntityList[i].name,
                    affiliation = seminarEntityList[i].affiliation,
                    location = seminarEntityList[i].location
                )
            )
        }

        return SeminarSearchResponse(total, seminarSearchDtoList)
    }
    override fun findPrevNextId(seminarId: Long, keyword: String?): Array<SeminarEntity?>? {
        val keywordBooleanBuilder = BooleanBuilder()

        if (!keyword.isNullOrEmpty()) {
            val keywordList = keyword.split("[^a-zA-Z0-9가-힣]".toRegex())
            keywordList.forEach {
                if (it.length == 1) {
                    throw CserealException.Csereal400("각각의 키워드는 한글자 이상이어야 합니다.")
                } else {
                    keywordBooleanBuilder.and(
                        seminarEntity.title.contains(it)
                            .or(seminarEntity.description.contains(it))
                    )
                }
            }
        }
        val seminarSearchDtoList = queryFactory.select(seminarEntity).from(seminarEntity)
            .where(seminarEntity.isDeleted.eq(false), seminarEntity.isPublic.eq(true))
            .where(keywordBooleanBuilder)
            .orderBy(seminarEntity.createdAt.desc())
            .fetch()

        val findingId = seminarSearchDtoList.indexOfFirst { it.id == seminarId }

        val prevNext: Array<SeminarEntity?>?

        if (findingId == -1) {
            return null
        } else if (findingId != 0 && findingId != seminarSearchDtoList.size - 1) {
            prevNext = arrayOf(seminarSearchDtoList[findingId + 1], seminarSearchDtoList[findingId - 1])
        } else if (findingId == 0) {
            if (seminarSearchDtoList.size == 1) {
                prevNext = arrayOf(null, null)
            } else {
                prevNext = arrayOf(seminarSearchDtoList[1], null)
            }
        } else {
            prevNext = arrayOf(null, seminarSearchDtoList[seminarSearchDtoList.size - 2])
        }

        return prevNext

    }
}