package com.wafflestudio.csereal.core.news.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.news.database.NewsEntity
import com.wafflestudio.csereal.core.news.database.NewsRepository
import com.wafflestudio.csereal.core.news.dto.NewsDto
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface NewsService {
    fun readNews(newsId: Long): NewsDto
}

@Service
class NewsServiceImpl(
    private val newsRepository: NewsRepository,
) : NewsService {

    @Transactional
    override fun readNews(newsId: Long): NewsDto {
        val news: NewsEntity = newsRepository.findByIdOrNull(newsId)
            ?: throw CserealException.Csereal404("존재하지 않는 새소식입니다.(newsId: $newsId)")

        if (news.isDeleted) throw CserealException.Csereal404("삭제된 새소식입니다.(newsId: $newsId")
        return NewsDto.of(news)
    }
}