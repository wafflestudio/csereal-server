package com.wafflestudio.csereal.core.notice.api.v2

import com.wafflestudio.csereal.common.enums.ContentSearchSortType
import com.wafflestudio.csereal.core.notice.dto.*
import com.wafflestudio.csereal.core.notice.service.NoticeService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.hibernate.validator.constraints.Length
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v2/notice")
@RestController
class NoticeController(
    private val noticeService: NoticeService
) {
    @GetMapping
    fun searchNotice(
        @RequestParam(required = false) tag: List<String>?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) @Positive pageNum: Int?,
        @RequestParam(required = false, defaultValue = "20") @Positive pageSize: Int,
        @RequestParam(required = false, defaultValue = "DATE") sortBy: String
    ): ResponseEntity<NoticeSearchResponse> {
        val usePageBtn = pageNum != null
        val page = pageNum ?: 1
        val pageRequest = PageRequest.of(page - 1, pageSize)

        val sortType = ContentSearchSortType.fromJsonValue(sortBy)

        return ResponseEntity.ok(noticeService.searchNotice(tag, keyword, pageRequest, usePageBtn, sortType))
    }

    @GetMapping("/totalSearch")
    fun totalSearchNotice(
        @RequestParam(required = true)
        @Length(min = 2)
        @NotBlank
        keyword: String,
        @RequestParam(required = true) @Positive number: Int,
        @RequestParam(required = false, defaultValue = "200") @Positive stringLength: Int
    ): NoticeTotalSearchResponse {
        return noticeService.searchTotalNotice(keyword, number, stringLength)
    }

    @GetMapping("/{noticeId}")
    fun readNotice(
        @PathVariable noticeId: Long
    ): ResponseEntity<NoticeDto> {
        return ResponseEntity.ok(noticeService.readNotice(noticeId))
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping
    fun createNotice(
        @Valid
        @RequestPart("request")
        request: NoticeDto,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ): ResponseEntity<NoticeDto> {
        return ResponseEntity.ok(noticeService.createNotice(request, attachments))
    }

    @PreAuthorize("hasRole('STAFF')")
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

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/{noticeId}")
    fun deleteNotice(
        @PathVariable noticeId: Long
    ) {
        noticeService.deleteNotice(noticeId)
    }

    @PreAuthorize("hasRole('STAFF')")
    @PatchMapping
    fun unpinManyNotices(
        @RequestBody request: NoticeIdListRequest
    ) {
        noticeService.unpinManyNotices(request.idList)
    }

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping
    fun deleteManyNotices(
        @RequestBody request: NoticeIdListRequest
    ) {
        noticeService.deleteManyNotices(request.idList)
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/tag")
    fun enrollTag(
        @RequestBody tagName: Map<String, String>
    ): ResponseEntity<String> {
        noticeService.enrollTag(tagName["name"]!!)
        return ResponseEntity<String>("등록되었습니다. (tagName: ${tagName["name"]})", HttpStatus.OK)
    }

    @GetMapping("/ids")
    fun getAllIds(): ResponseEntity<List<Long>> {
        return ResponseEntity.ok(noticeService.getAllIds())
    }
}
