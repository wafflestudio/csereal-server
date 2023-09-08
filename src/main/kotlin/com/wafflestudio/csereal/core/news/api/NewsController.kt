package com.wafflestudio.csereal.core.news.api

import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.news.dto.NewsSearchResponse
import com.wafflestudio.csereal.core.news.service.NewsService
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v1/news")
@RestController
class NewsController(
    private val newsService: NewsService,
) {
    @GetMapping
    fun searchNews(
        @RequestParam(required = false) tag: List<String>?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false, defaultValue = "1") pageNum: Int
    ): ResponseEntity<NewsSearchResponse> {
        val pageSize = 10
        val pageRequest = PageRequest.of(pageNum - 1, pageSize)
        val usePageBtn = pageNum != 1
        return ResponseEntity.ok(newsService.searchNews(tag, keyword, pageRequest, usePageBtn))
    }

    @GetMapping("/{newsId}")
    fun readNews(
        @PathVariable newsId: Long
    ): ResponseEntity<NewsDto> {
        return ResponseEntity.ok(newsService.readNews(newsId))
    }

    @PostMapping
    fun createNews(
        @Valid @RequestPart("request") request: NewsDto,
        @RequestPart("mainImage") mainImage: MultipartFile?,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ): ResponseEntity<NewsDto> {
        return ResponseEntity.ok(newsService.createNews(request, mainImage, attachments))
    }

    @PatchMapping("/{newsId}")
    fun updateNews(
        @PathVariable newsId: Long,
        @Valid @RequestPart("request") request: NewsDto,
        @RequestPart("mainImage") mainImage: MultipartFile?,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ): ResponseEntity<NewsDto> {
        return ResponseEntity.ok(newsService.updateNews(newsId, request, mainImage, attachments))
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
    ): ResponseEntity<String> {
        newsService.enrollTag(tagName["name"]!!)
        return ResponseEntity<String>("등록되었습니다. (tagName: ${tagName["name"]})", HttpStatus.OK)
    }


}
