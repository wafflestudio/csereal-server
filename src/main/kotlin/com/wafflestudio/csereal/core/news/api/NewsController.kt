package com.wafflestudio.csereal.core.news.api

import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.news.service.NewsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/news")
class NewsController(
    private val newsService: NewsService,
) {
    @GetMapping("/{newsId}")
    fun readNews(
        @PathVariable newsId: Long,
    ) : NewsDto {
        return newsService.readNews(newsId)
    }
}