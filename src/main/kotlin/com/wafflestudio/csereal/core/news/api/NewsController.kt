package com.wafflestudio.csereal.core.news.api

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.news.dto.NewsSearchResponse
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
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
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
        @AuthenticationPrincipal oidcUser: OidcUser?
    ): ResponseEntity<NewsSearchResponse> {
        val isStaff = oidcUser?.let {
            val username = it.idToken.getClaim<String>("username")
            val user = userRepository.findByUsername(username)
            user?.role == Role.ROLE_STAFF
        } ?: false

        val pageSize = 10
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
        @RequestParam(required = false, defaultValue = "200") @Positive stringLength: Int
    ) = ResponseEntity.ok(
        newsService.searchTotalNews(keyword, number, stringLength)
    )

    @GetMapping("/{newsId}")
    fun readNews(
        @PathVariable newsId: Long
    ): ResponseEntity<NewsDto> {
        return ResponseEntity.ok(newsService.readNews(newsId))
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
