package com.wafflestudio.csereal.core.seminar.database

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.seminar.database.QSeminarEntity.seminarEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

interface SeminarRepository : JpaRepository<SeminarEntity, Long>, CustomSeminarRepository {
}

interface CustomSeminarRepository {
    fun findPrevNextId(seminarId: Long, keyword: String?): Array<SeminarEntity?>?
}

@Component
class SeminarRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CustomSeminarRepository {
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
            .distinct()
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