package com.wafflestudio.csereal.core.news.database

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.utils.FixedPageRequest
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import com.wafflestudio.csereal.core.news.database.QNewsEntity.newsEntity
import com.wafflestudio.csereal.core.news.database.QNewsTagEntity.newsTagEntity
import com.wafflestudio.csereal.core.news.dto.NewsSearchDto
import com.wafflestudio.csereal.core.news.dto.NewsSearchResponse
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime

interface NewsRepository : JpaRepository<NewsEntity, Long>, CustomNewsRepository {
    fun findAllByIsImportant(isImportant: Boolean): List<NewsEntity>
    fun findFirstByCreatedAtLessThanOrderByCreatedAtDesc(timestamp: LocalDateTime): NewsEntity?
    fun findFirstByCreatedAtGreaterThanOrderByCreatedAtAsc(timestamp: LocalDateTime): NewsEntity?
}

interface CustomNewsRepository {
    fun searchNews(tag: List<String>?, keyword: String?, pageable: Pageable, usePageBtn: Boolean): NewsSearchResponse
}

@Component
class NewsRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val mainImageService: MainImageService,
) : CustomNewsRepository {
    override fun searchNews(
        tag: List<String>?,
        keyword: String?,
        pageable: Pageable,
        usePageBtn: Boolean
    ): NewsSearchResponse {
        val keywordBooleanBuilder = BooleanBuilder()
        val tagsBooleanBuilder = BooleanBuilder()

        if (!keyword.isNullOrEmpty()) {
            val keywordList = keyword.split("[^a-zA-Z0-9가-힣]".toRegex())
            keywordList.forEach {
                if (it.length == 1) {
                    throw CserealException.Csereal400("각각의 키워드는 한글자 이상이어야 합니다.")
                } else {
                    keywordBooleanBuilder.and(
                        newsEntity.title.contains(it)
                            .or(newsEntity.description.contains(it))
                    )
                }
            }
        }
        if (!tag.isNullOrEmpty()) {
            tag.forEach {
                val tagEnum = TagInNewsEnum.getTagEnum(it)
                tagsBooleanBuilder.or(
                    newsTagEntity.tag.name.eq(tagEnum)
                )
            }
        }

        val jpaQuery = queryFactory.selectFrom(newsEntity)
            .leftJoin(newsTagEntity).on(newsTagEntity.news.eq(newsEntity))
            .where(newsEntity.isDeleted.eq(false))
            .where(keywordBooleanBuilder).where(tagsBooleanBuilder)

        val total: Long
        var pageRequest = pageable

        if (usePageBtn) {
            val countQuery = jpaQuery.clone()
            total = countQuery.select(newsEntity.countDistinct()).fetchOne()!!
            pageRequest = FixedPageRequest(pageable, total)
        } else {
            total = (10 * pageable.pageSize).toLong() // 10개 페이지 고정
        }

        val newsEntityList = jpaQuery
            .orderBy(newsEntity.createdAt.desc())
            .offset(pageRequest.offset)
            .limit(pageRequest.pageSize.toLong())
            .distinct()
            .fetch()

        val newsSearchDtoList: List<NewsSearchDto> = newsEntityList.map {
            val imageURL = mainImageService.createImageURL(it.mainImage)
            NewsSearchDto(
                id = it.id,
                title = it.title,
                description = it.plainTextDescription,
                createdAt = it.createdAt,
                tags = it.newsTags.map { newsTagEntity -> TagInNewsEnum.getTagString(newsTagEntity.tag.name) },
                imageURL = imageURL
            )
        }
        return NewsSearchResponse(total!!, newsSearchDtoList)
    }
}
