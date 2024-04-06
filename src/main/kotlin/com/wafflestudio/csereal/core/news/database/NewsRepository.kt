package com.wafflestudio.csereal.core.news.database

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.enums.ContentSearchSortType
import com.wafflestudio.csereal.common.repository.CommonRepository
import com.wafflestudio.csereal.common.utils.FixedPageRequest
import com.wafflestudio.csereal.core.admin.dto.AdminSlideElement
import com.wafflestudio.csereal.core.admin.dto.AdminSlidesResponse
import com.wafflestudio.csereal.core.news.database.QNewsEntity.newsEntity
import com.wafflestudio.csereal.core.news.database.QNewsTagEntity.newsTagEntity
import com.wafflestudio.csereal.core.news.database.QTagInNewsEntity.tagInNewsEntity
import com.wafflestudio.csereal.core.news.dto.NewsSearchDto
import com.wafflestudio.csereal.core.news.dto.NewsSearchResponse
import com.wafflestudio.csereal.core.news.dto.NewsTotalSearchDto
import com.wafflestudio.csereal.core.news.dto.NewsTotalSearchElement
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.QMainImageEntity.mainImageEntity
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

interface NewsRepository : JpaRepository<NewsEntity, Long>, CustomNewsRepository {
    fun findAllByIsPrivateFalseAndIsImportantTrueAndIsDeletedFalse(): List<NewsEntity>
    fun findAllByIsImportantTrueAndIsDeletedFalse(): List<NewsEntity>
    fun findFirstByIsDeletedFalseAndIsPrivateFalseAndCreatedAtLessThanOrderByCreatedAtDesc(
        timestamp: LocalDateTime
    ): NewsEntity?

    fun findFirstByIsDeletedFalseAndIsPrivateFalseAndCreatedAtGreaterThanOrderByCreatedAtAsc(
        timestamp: LocalDateTime
    ): NewsEntity?
}

interface CustomNewsRepository {
    fun searchNews(
        tag: List<String>?,
        keyword: String?,
        pageable: Pageable,
        usePageBtn: Boolean,
        sortBy: ContentSearchSortType,
        isStaff: Boolean
    ): NewsSearchResponse

    fun searchTotalNews(
        keyword: String,
        number: Int,
        amount: Int,
        imageUrlCreator: (MainImageEntity?) -> String?,
        isStaff: Boolean
    ): NewsTotalSearchDto

    fun readAllSlides(pageNum: Long, pageSize: Int): AdminSlidesResponse
}

@Repository
class NewsRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val mainImageService: MainImageService,
    private val commonRepository: CommonRepository
) : CustomNewsRepository {
    override fun searchNews(
        tag: List<String>?,
        keyword: String?,
        pageable: Pageable,
        usePageBtn: Boolean,
        sortBy: ContentSearchSortType,
        isStaff: Boolean
    ): NewsSearchResponse {
        val keywordBooleanBuilder = BooleanBuilder()
        val tagsBooleanBuilder = BooleanBuilder()
        val isPrivateBooleanBuilder = BooleanBuilder()

        if (!keyword.isNullOrEmpty()) {
            val booleanTemplate = commonRepository.searchFullDoubleTextTemplate(
                keyword,
                newsEntity.title,
                newsEntity.plainTextDescription
            )
            keywordBooleanBuilder.and(booleanTemplate.gt(0.0))
        }
        if (!tag.isNullOrEmpty()) {
            tag.forEach {
                val tagEnum = TagInNewsEnum.getTagEnum(it)
                tagsBooleanBuilder.or(
                    newsTagEntity.tag.name.eq(tagEnum)
                )
            }
        }

        if (!isStaff) {
            isPrivateBooleanBuilder.or(
                newsEntity.isPrivate.eq(false)
            )
        }

        val jpaQuery = queryFactory.selectFrom(newsEntity)
            .leftJoin(newsTagEntity).on(newsTagEntity.news.eq(newsEntity))
            .where(newsEntity.isDeleted.eq(false))
            .where(keywordBooleanBuilder, tagsBooleanBuilder, isPrivateBooleanBuilder)

        val total: Long
        var pageRequest = pageable

        if (usePageBtn) {
            val countQuery = jpaQuery.clone()
            total = countQuery.select(newsEntity.countDistinct()).fetchOne()!!
            pageRequest = FixedPageRequest(pageable, total)
        } else {
            total = (10 * pageable.pageSize).toLong() + 1 // 10개 페이지 고정
        }

        val newsEntityQuery = jpaQuery
            .offset(pageRequest.offset)
            .limit(pageRequest.pageSize.toLong())
            .distinct()

        val newsEntityList = when {
            sortBy == ContentSearchSortType.DATE || keyword.isNullOrEmpty() -> newsEntityQuery.orderBy(
                newsEntity.createdAt.desc()
            )
            else /* sortBy == RELEVANCE */ -> newsEntityQuery
        }.fetch()

        val newsSearchDtoList: List<NewsSearchDto> = newsEntityList.map {
            val imageURL = mainImageService.createImageURL(it.mainImage)
            NewsSearchDto(
                id = it.id,
                title = it.title,
                description = it.plainTextDescription,
                createdAt = it.createdAt,
                date = it.date,
                tags = it.newsTags.map { newsTagEntity -> newsTagEntity.tag.name.krName },
                imageURL = imageURL,
                isPrivate = it.isPrivate
            )
        }
        return NewsSearchResponse(total, newsSearchDtoList)
    }

    override fun searchTotalNews(
        keyword: String,
        number: Int,
        amount: Int,
        imageUrlCreator: (MainImageEntity?) -> String?,
        isStaff: Boolean
    ): NewsTotalSearchDto {
        val doubleTemplate = commonRepository.searchFullDoubleTextTemplate(
            keyword,
            newsEntity.title,
            newsEntity.plainTextDescription
        )

        val privateBoolean = newsEntity.isPrivate.eq(false).takeUnless { isStaff }

        val searchResult = queryFactory.select(
            newsEntity.id,
            newsEntity.title,
            newsEntity.date,
            newsEntity.plainTextDescription,
            mainImageEntity
        ).from(newsEntity)
            .leftJoin(mainImageEntity).on(newsEntity.mainImage.eq(mainImageEntity))
            .where(doubleTemplate.gt(0.0), privateBoolean)
            .orderBy(newsEntity.date.desc())
            .limit(number.toLong())
            .fetch()

        val searchResultTags = queryFactory.select(
            newsTagEntity.news.id,
            newsTagEntity.tag.name
        ).from(newsTagEntity)
            .rightJoin(newsEntity).on(newsTagEntity.news.eq(newsEntity))
            .leftJoin(tagInNewsEntity).on(newsTagEntity.tag.eq(tagInNewsEntity))
            .where(newsTagEntity.news.id.`in`(searchResult.map { it[newsEntity.id] }))
            .distinct()
            .fetch()

        val total = queryFactory.select(newsEntity.countDistinct())
            .from(newsEntity)
            .where(doubleTemplate.gt(0.0))
            .fetchOne()!!

        return NewsTotalSearchDto(
            total.toInt(),
            searchResult.map {
                NewsTotalSearchElement(
                    id = it[newsEntity.id]!!,
                    title = it[newsEntity.title]!!,
                    date = it[newsEntity.date],
                    tags = searchResultTags.filter { tag ->
                        tag[newsTagEntity.news.id] == it[newsEntity.id]
                    }.map { tag ->
                        tag[newsTagEntity.tag.name]!!.krName
                    },
                    imageUrl = imageUrlCreator(it[mainImageEntity]),
                    description = it[newsEntity.plainTextDescription]!!,
                    keyword = keyword,
                    amount = amount
                )
            }
        )
    }

    override fun readAllSlides(pageNum: Long, pageSize: Int): AdminSlidesResponse {
        val tuple = queryFactory.select(
            newsEntity.id,
            newsEntity.title,
            newsEntity.createdAt
        ).from(newsEntity)
            .where(newsEntity.isDeleted.eq(false), newsEntity.isPrivate.eq(false), newsEntity.isSlide.eq(true))
            .orderBy(newsEntity.createdAt.desc())
            .offset(pageSize * pageNum)
            .limit(pageSize.toLong())
            .fetch()

        val total = queryFactory.select(newsEntity.count())
            .from(newsEntity)
            .where(newsEntity.isDeleted.eq(false), newsEntity.isPrivate.eq(false), newsEntity.isSlide.eq(true))
            .fetchOne()!!

        return AdminSlidesResponse(
            total,
            tuple.map {
                AdminSlideElement(
                    id = it[newsEntity.id]!!,
                    title = it[newsEntity.title]!!,
                    createdAt = it[newsEntity.createdAt]!!
                )
            }
        )
    }
}
