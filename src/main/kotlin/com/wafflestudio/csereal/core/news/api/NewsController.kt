package com.wafflestudio.csereal.core.news.api

import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.news.dto.NewsSearchResponse
import com.wafflestudio.csereal.core.news.service.NewsService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/news")
@RestController
class NewsController(
    private val newsService: NewsService,
) {
    @GetMapping
    fun searchNews(
        @RequestParam(required = false) tag: List<String>?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false, defaultValue = "0") pageNum:Long
    ) : ResponseEntity<NewsSearchResponse> {
        return ResponseEntity.ok(newsService.searchNews(tag, keyword, pageNum))
    }
    @GetMapping("/{newsId}")
    fun readNews(
        @PathVariable newsId: Long,
        @RequestParam(required = false) tag : List<String>?,
        @RequestParam(required = false) keyword: String?,
    ) : ResponseEntity<NewsDto> {
        return ResponseEntity.ok(newsService.readNews(newsId, tag, keyword))
    }

    @PostMapping
    fun createNews(
        @Valid @RequestPart("request") request: NewsDto,
        @RequestPart("image") image: MultipartFile?,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ) : ResponseEntity<NewsDto> {
        return ResponseEntity.ok(newsService.createNews(request,image, attachments))
    }

    @PatchMapping("/{newsId}")
    fun updateNews(
        @PathVariable newsId: Long,
        @Valid @RequestBody request: NewsDto,
    ) : ResponseEntity<NewsDto> {
        return ResponseEntity.ok(newsService.updateNews(newsId, request))
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