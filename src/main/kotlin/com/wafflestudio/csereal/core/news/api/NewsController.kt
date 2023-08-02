package com.wafflestudio.csereal.core.news.api

import com.wafflestudio.csereal.core.news.dto.CreateNewsRequest
import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.news.dto.UpdateNewsRequest
import com.wafflestudio.csereal.core.news.service.NewsService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/news")
@RestController
class NewsController(
    private val newsService: NewsService,
) {
    @GetMapping("/{newsId}")
    fun readNews(
        @PathVariable newsId: Long,
    ) : NewsDto {
        return newsService.readNews(newsId)
    }

    @PostMapping
    fun createNews(
        @Valid @RequestBody request: CreateNewsRequest
    ) : NewsDto {
        return newsService.createNews(request)
    }

    @PatchMapping("/{newsId}")
    fun updateNews(
        @PathVariable newsId: Long,
        @Valid @RequestBody request: UpdateNewsRequest,
    ) : NewsDto {
        return newsService.updateNews(newsId, request)
    }

    @DeleteMapping("/{newsId}")
    fun deleteNews(
        @PathVariable newsId: Long
    ) {
        newsService.deleteNews(newsId)
    }

    @PostMapping("/tag")
    fun enrollTag(
        @RequestBody tagName: Map<String, String>
    ) : ResponseEntity<String> {
        newsService.enrollTag(tagName["name"]!!)
        return ResponseEntity<String>("등록되었습니다. (tagName: ${tagName["name"]})", HttpStatus.OK)
    }


}