package com.wafflestudio.csereal.core.news.api

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.common.utils.getUsername
import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.news.dto.NewsSearchResponse
import com.wafflestudio.csereal.core.news.dto.NewsTotalSearchDto
import com.wafflestudio.csereal.core.news.service.NewsService
import com.wafflestudio.csereal.core.user.database.Role
import com.wafflestudio.csereal.core.user.database.UserRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.hibernate.validator.constraints.Length
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v1/news")
@RestController
class NewsController(
    private val newsService: NewsService,
    private val userRepository: UserRepository
) {
    @GetMapping
    fun searchNews(
        @RequestParam(required = false) tag: List<String>?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) pageNum: Int?,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int,
        authentication: Authentication?
    ): ResponseEntity<NewsSearchResponse> {
        val username = getUsername(authentication)
        val isStaff = username?.let {
            val user = userRepository.findByUsername(it)
            user?.role == Role.ROLE_STAFF
        } ?: false

        val usePageBtn = pageNum != null
        val page = pageNum ?: 1
        val pageRequest = PageRequest.of(page - 1, pageSize)
        return ResponseEntity.ok(newsService.searchNews(tag, keyword, pageRequest, usePageBtn, isStaff))
    }

    @GetMapping("/totalSearch")
    fun searchTotalNews(
        @RequestParam(required = true)
        @Length(min = 1)
        @NotBlank
        keyword: String,
        @RequestParam(required = true) @Positive number: Int,
        @RequestParam(required = false, defaultValue = "200") @Positive stringLength: Int,
        authentication: Authentication?
    ): NewsTotalSearchDto {
        val username = getUsername(authentication)
        val isStaff = username?.let {
            val user = userRepository.findByUsername(it)
            user?.role == Role.ROLE_STAFF
        } ?: false

        return newsService.searchTotalNews(keyword, number, stringLength, isStaff)
    }

    @GetMapping("/{newsId}")
    fun readNews(
        @PathVariable newsId: Long,
        authentication: Authentication?
    ): ResponseEntity<NewsDto> {
        val username = getUsername(authentication)
        val isStaff = username?.let {
            val user = userRepository.findByUsername(it)
            user?.role == Role.ROLE_STAFF
        } ?: false
        return ResponseEntity.ok(newsService.readNews(newsId, isStaff))
    }

    @AuthenticatedStaff
    @PostMapping
    fun createNews(
        @Valid
        @RequestPart("request")
        request: NewsDto,
        @RequestPart("mainImage") mainImage: MultipartFile?,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ): ResponseEntity<NewsDto> {
        return ResponseEntity.ok(newsService.createNews(request, mainImage, attachments))
    }

    @AuthenticatedStaff
    @PatchMapping("/{newsId}")
    fun updateNews(
        @PathVariable newsId: Long,
        @Valid
        @RequestPart("request")
        request: NewsDto,
        @RequestPart("newMainImage") newMainImage: MultipartFile?,
        @RequestPart("newAttachments") newAttachments: List<MultipartFile>?
    ): ResponseEntity<NewsDto> {
        return ResponseEntity.ok(newsService.updateNews(newsId, request, newMainImage, newAttachments))
    }

    @AuthenticatedStaff
    @DeleteMapping("/{newsId}")
    fun deleteNews(
        @PathVariable newsId: Long
    ) {
        newsService.deleteNews(newsId)
    }

    @AuthenticatedStaff
    @PostMapping("/tag")
    fun enrollTag(
        @RequestBody tagName: Map<String, String>
    ): ResponseEntity<String> {
        newsService.enrollTag(tagName["name"]!!)
        return ResponseEntity<String>("등록되었습니다. (tagName: ${tagName["name"]})", HttpStatus.OK)
    }
}
