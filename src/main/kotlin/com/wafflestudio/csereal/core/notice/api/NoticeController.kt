package com.wafflestudio.csereal.core.notice.api

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.notice.dto.*
import com.wafflestudio.csereal.core.notice.service.NoticeService
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v1/notice")
@RestController
class NoticeController(
    private val noticeService: NoticeService,
) {
    @GetMapping
    fun searchNotice(
        @RequestParam(required = false) tag: List<String>?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false, defaultValue = "1") pageNum: Int
    ): ResponseEntity<NoticeSearchResponse> {
        val pageSize = 20
        val pageRequest = PageRequest.of(pageNum - 1, pageSize)
        val usePageBtn = pageNum != 1
        return ResponseEntity.ok(noticeService.searchNotice(tag, keyword, pageRequest, usePageBtn))
    }

    @GetMapping("/{noticeId}")
    fun readNotice(
        @PathVariable noticeId: Long
    ): ResponseEntity<NoticeDto> {
        return ResponseEntity.ok(noticeService.readNotice(noticeId))
    }

    @AuthenticatedStaff
    @PostMapping
    fun createNotice(
        @Valid @RequestPart("request") request: NoticeDto,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ): ResponseEntity<NoticeDto> {
        return ResponseEntity.ok(noticeService.createNotice(request, attachments))
    }

    @PatchMapping("/{noticeId}")
    fun updateNotice(
        @PathVariable noticeId: Long,
        @Valid @RequestPart("request") request: NoticeDto,
        @RequestPart("attachments") attachments: List<MultipartFile>?,
    ): ResponseEntity<NoticeDto> {
        return ResponseEntity.ok(noticeService.updateNotice(noticeId, request, attachments))
    }

    @DeleteMapping("/{noticeId}")
    fun deleteNotice(
        @PathVariable noticeId: Long
    ) {
        noticeService.deleteNotice(noticeId)
    }

    @PatchMapping
    fun unpinManyNotices(
        @RequestBody request: NoticeIdListRequest
    ) {
        noticeService.unpinManyNotices(request.idList)
    }

    @DeleteMapping
    fun deleteManyNotices(
        @RequestBody request: NoticeIdListRequest
    ) {
        noticeService.deleteManyNotices(request.idList)
    }

    @PostMapping("/tag")
    fun enrollTag(
        @RequestBody tagName: Map<String, String>
    ): ResponseEntity<String> {
        noticeService.enrollTag(tagName["name"]!!)
        return ResponseEntity<String>("등록되었습니다. (tagName: ${tagName["name"]})", HttpStatus.OK)
    }
}
