package com.wafflestudio.csereal.core.notice.api

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.notice.dto.*
import com.wafflestudio.csereal.core.notice.service.NoticeService
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

@RequestMapping("/api/v1/notice")
@RestController
class NoticeController(
    private val noticeService: NoticeService,
    private val userRepository: UserRepository
) {
    @GetMapping
    fun searchNotice(
        @RequestParam(required = false) tag: List<String>?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) pageNum: Int?,
        @AuthenticationPrincipal oidcUser: OidcUser?
    ): ResponseEntity<NoticeSearchResponse> {
        val isStaff = oidcUser?.let {
            val username = it.idToken.getClaim<String>("username")
            val user = userRepository.findByUsername(username)
            user?.role == Role.ROLE_STAFF
        } ?: false

        val usePageBtn = pageNum != null
        val page = pageNum ?: 1
        val pageSize = 20
        val pageRequest = PageRequest.of(page - 1, pageSize)
        return ResponseEntity.ok(noticeService.searchNotice(tag, keyword, pageRequest, usePageBtn, isStaff))
    }

    @GetMapping("/totalSearch")
    fun totalSearchNotice(
        @RequestParam(required = true)
        @Length(min = 2)
        @NotBlank
        keyword: String,
        @RequestParam(required = true) @Positive number: Int,
        @RequestParam(required = false, defaultValue = "200") @Positive stringLength: Int
    ) = ResponseEntity.ok(
        noticeService.searchTotalNotice(keyword, number, stringLength)
    )

    @GetMapping("/{noticeId}")
    fun readNotice(
        @PathVariable noticeId: Long
    ): ResponseEntity<NoticeDto> {
        return ResponseEntity.ok(noticeService.readNotice(noticeId))
    }

    @AuthenticatedStaff
    @PostMapping
    fun createNotice(
        @Valid
        @RequestPart("request")
        request: NoticeDto,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ): ResponseEntity<NoticeDto> {
        return ResponseEntity.ok(noticeService.createNotice(request, attachments))
    }

    @AuthenticatedStaff
    @PatchMapping("/{noticeId}")
    fun updateNotice(
        @PathVariable noticeId: Long,
        @Valid
        @RequestPart("request")
        request: NoticeDto,
        @RequestPart("newAttachments") newAttachments: List<MultipartFile>?
    ): ResponseEntity<NoticeDto> {
        return ResponseEntity.ok(noticeService.updateNotice(noticeId, request, newAttachments))
    }

    @AuthenticatedStaff
    @DeleteMapping("/{noticeId}")
    fun deleteNotice(
        @PathVariable noticeId: Long
    ) {
        noticeService.deleteNotice(noticeId)
    }

    @AuthenticatedStaff
    @PatchMapping
    fun unpinManyNotices(
        @RequestBody request: NoticeIdListRequest
    ) {
        noticeService.unpinManyNotices(request.idList)
    }

    @AuthenticatedStaff
    @DeleteMapping
    fun deleteManyNotices(
        @RequestBody request: NoticeIdListRequest
    ) {
        noticeService.deleteManyNotices(request.idList)
    }

    @AuthenticatedStaff
    @PostMapping("/tag")
    fun enrollTag(
        @RequestBody tagName: Map<String, String>
    ): ResponseEntity<String> {
        noticeService.enrollTag(tagName["name"]!!)
        return ResponseEntity<String>("등록되었습니다. (tagName: ${tagName["name"]})", HttpStatus.OK)
    }
}
