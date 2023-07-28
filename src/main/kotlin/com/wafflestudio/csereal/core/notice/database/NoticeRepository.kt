package com.wafflestudio.csereal.core.notice.database

import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.core.notice.dto.NoticeDto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

interface NoticeRepository : JpaRepository<NoticeEntity, Long>, CustomNoticeRepository {
}

interface CustomNoticeRepository {
    fun searchNotice(tag: List<Long>, keyword: String) : List<NoticeDto>
}
@Component
class NoticeRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : CustomNoticeRepository {
    override fun searchNotice(tag: List<Long>, keyword: String): List<NoticeDto> {
        TODO("Not yet implemented")
    }

}