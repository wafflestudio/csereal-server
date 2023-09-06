package com.wafflestudio.csereal.core.news.database

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.cleanTextFromHtml
import com.wafflestudio.csereal.core.news.database.QNewsEntity.newsEntity
import com.wafflestudio.csereal.core.news.database.QNewsTagEntity.newsTagEntity
import com.wafflestudio.csereal.core.news.dto.NewsSearchDto
import com.wafflestudio.csereal.core.news.dto.NewsSearchResponse
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.jsoup.safety.Safelist
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

interface NewsRepository : JpaRepository<NewsEntity, Long>, CustomNewsRepository {
    fun findAllByIsImportant(isImportant: Boolean): List<NewsEntity>
}

interface CustomNewsRepository {
    fun searchNews(tag: List<String>?, keyword: String?, pageNum: Long): NewsSearchResponse
    fun findPrevNextId(newsId: Long, tag: List<String>?, keyword: String?): Array<NewsEntity?>?
}

@Component
class NewsRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val mainImageService: MainImageService,
) : CustomNewsRepository {
    override fun searchNews(tag: List<String>?, keyword: String?, pageNum: Long): NewsSearchResponse {
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
                tagsBooleanBuilder.or(
                    newsTagEntity.tag.name.eq(it)
                )
            }
        }

        val jpaQuery = queryFactory.select(newsEntity).from(newsEntity)
            .leftJoin(newsTagEntity).on(newsTagEntity.news.eq(newsEntity))
            .where(newsEntity.isDeleted.eq(false), newsEntity.isPublic.eq(true))
            .where(keywordBooleanBuilder).where(tagsBooleanBuilder)

        val countQuery = jpaQuery.clone()
        val total = countQuery.select(newsEntity.countDistinct()).fetchOne()

        val newsEntityList = jpaQuery
            .orderBy(newsEntity.createdAt.desc())
            .offset(20*pageNum)
            .limit(20)
            .distinct()
            .fetch()

        val newsSearchDtoList : List<NewsSearchDto> = newsEntityList.map {
            val imageURL = mainImageService.createImageURL(it.mainImage)
            NewsSearchDto(
                id = it.id,
                title = it.title,
                description = cleanTextFromHtml(it.description),
                createdAt = it.createdAt,
                tags = it.newsTags.map { newsTagEntity -> newsTagEntity.tag.name },
                imageURL = imageURL
            )
        }
        return NewsSearchResponse(total!!, newsSearchDtoList)
    }

    override fun findPrevNextId(newsId: Long, tag: List<String>?, keyword: String?): Array<NewsEntity?>? {
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
                tagsBooleanBuilder.or(
                    newsTagEntity.tag.name.eq(it)
                )
            }
        }

        val newsSearchDtoList = queryFactory.select(newsEntity).from(newsEntity)
            .leftJoin(newsTagEntity).on(newsTagEntity.news.eq(newsEntity))
            .where(newsEntity.isDeleted.eq(false), newsEntity.isPublic.eq(true))
            .where(keywordBooleanBuilder).where(tagsBooleanBuilder)
            .orderBy(newsEntity.createdAt.desc())
            .distinct()
            .fetch()


        val findingId = newsSearchDtoList.indexOfFirst { it.id == newsId }

        val prevNext: Array<NewsEntity?>?
        if (findingId == -1) {
            prevNext = null
        } else if (findingId != 0 && findingId != newsSearchDtoList.size - 1) {
            prevNext = arrayOf(newsSearchDtoList[findingId + 1], newsSearchDtoList[findingId - 1])
        } else if (findingId == 0) {
            if (newsSearchDtoList.size == 1) {
                prevNext = arrayOf(null, null)
            } else {
                prevNext = arrayOf(newsSearchDtoList[1], null)
            }
        } else {
            prevNext = arrayOf(null, newsSearchDtoList[newsSearchDtoList.size - 2])
        }

        return prevNext
    }
}
