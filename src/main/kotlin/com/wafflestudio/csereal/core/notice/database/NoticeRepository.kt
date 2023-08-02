package com.wafflestudio.csereal.core.notice.database

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.notice.database.QNoticeEntity.noticeEntity
import com.wafflestudio.csereal.core.notice.database.QNoticeTagEntity.noticeTagEntity
import com.wafflestudio.csereal.core.notice.dto.NoticeSearchDto
import com.wafflestudio.csereal.core.notice.dto.NoticeSearchResponse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

interface NoticeRepository : JpaRepository<NoticeEntity, Long>, CustomNoticeRepository {
}

interface CustomNoticeRepository {
    fun searchNotice(tag: List<String>?, keyword: String?, pageNum: Long): NoticeSearchResponse
    fun findPrevNextId(noticeId: Long, tag: List<String>?, keyword: String?): Array<Long?>?
}

@Component
class NoticeRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CustomNoticeRepository {
    override fun searchNotice(tag: List<String>?, keyword: String?, pageNum: Long): NoticeSearchResponse {
        val keywordBooleanBuilder = BooleanBuilder()
        val tagsBooleanBuilder = BooleanBuilder()

        if (!keyword.isNullOrEmpty()) {

            val keywordList = keyword.split("[^a-zA-Z0-9가-힣]".toRegex())
            keywordList.forEach {
                if (it.length == 1) {
                    throw CserealException.Csereal400("각각의 키워드는 한글자 이상이어야 합니다.")
                } else {
                    keywordBooleanBuilder.and(
                        noticeEntity.title.contains(it)
                            .or(noticeEntity.description.contains(it))
                    )
                }
            }

        }
        if (!tag.isNullOrEmpty()) {
            tag.forEach {
                tagsBooleanBuilder.or(
                    noticeTagEntity.tag.name.eq(it)
                )
            }
        }

        val jpaQuery = queryFactory.select(noticeEntity).from(noticeEntity)
            .leftJoin(noticeTagEntity).on(noticeTagEntity.notice.eq(noticeEntity))
            .where(noticeEntity.isDeleted.eq(false), noticeEntity.isPublic.eq(true))
            .where(keywordBooleanBuilder).where(tagsBooleanBuilder)

        val total = jpaQuery.distinct().fetch().size

        val noticeEntityList = jpaQuery.orderBy(noticeEntity.isPinned.desc())
            .orderBy(noticeEntity.createdAt.desc())
            .offset(20*pageNum)  //로컬 테스트를 위해 잠시 5로 둘 것, 원래는 20
            .limit(20)
            .distinct()
            .fetch()

        val noticeSearchDtoList : List<NoticeSearchDto> = noticeEntityList.map {
            NoticeSearchDto(
                id = it.id,
                title = it.title,
                createdAt = it.createdAt,
                isPinned = it.isPinned,
            )
        }

        return NoticeSearchResponse(total, noticeSearchDtoList)
    }

    override fun findPrevNextId(noticeId: Long, tag: List<String>?, keyword: String?): Array<Long?>? {
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
                    noticeTagEntity.tag.name.eq(it)
                )
            }
        }

        val noticeSearchDtoIdList = queryFactory.select(noticeEntity.id).from(noticeEntity)
            .leftJoin(noticeTagEntity).on(noticeTagEntity.notice.eq(noticeEntity))
            .where(noticeEntity.isDeleted.eq(false), noticeEntity.isPublic.eq(true))
            .where(keywordBooleanBuilder).where(tagsBooleanBuilder)
            .orderBy(noticeEntity.isPinned.desc())
            .orderBy(noticeEntity.createdAt.desc())
            .distinct()
            .fetch()

        val findingId = noticeSearchDtoIdList.indexOf(noticeId)

        val prevNext : Array<Long?>?
        if(findingId == -1) {
            return null
        } else if(findingId != 0 && findingId != noticeSearchDtoIdList.size-1) {
            prevNext = arrayOf(noticeSearchDtoIdList[findingId+1], noticeSearchDtoIdList[findingId-1])
        } else if(findingId == 0) {
            prevNext = arrayOf(noticeSearchDtoIdList[1],null)
        } else {
            prevNext = arrayOf(null, noticeSearchDtoIdList[noticeSearchDtoIdList.size-2])
        }

        return prevNext

    }

}