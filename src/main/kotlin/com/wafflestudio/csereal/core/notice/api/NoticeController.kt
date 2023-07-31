package com.wafflestudio.csereal.core.notice.api

import com.wafflestudio.csereal.core.notice.dto.*
import com.wafflestudio.csereal.core.notice.service.NoticeService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/notice")
@RestController
class NoticeController(
    private val noticeService: NoticeService,
) {
    @GetMapping
    fun searchNotice(
        @RequestParam(required = false) tag : List<Long>?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false, defaultValue = "0") pageNum: Long
    ): ResponseEntity<SearchResponse> {
        return ResponseEntity.ok(noticeService.searchNotice(tag, keyword, pageNum))
    }
    @GetMapping("/{noticeId}")
    fun readNotice(
        @PathVariable noticeId: Long,
    ) : ResponseEntity<NoticeDto> {
        return ResponseEntity.ok(noticeService.readNotice(noticeId))
    }

    @PostMapping
    fun createNotice(
        @Valid @RequestBody request: CreateNoticeRequest
    ) : ResponseEntity<NoticeDto> {
        return ResponseEntity.ok(noticeService.createNotice(request))
    }

    @PatchMapping("/{noticeId}")
    fun updateNotice(
        @PathVariable noticeId: Long,
        @Valid @RequestBody request: UpdateNoticeRequest,
    ) : ResponseEntity<NoticeDto> {
        return ResponseEntity.ok(noticeService.updateNotice(noticeId, request))
    }

    @DeleteMapping("/{noticeId}")
    fun deleteNotice(
        @PathVariable noticeId: Long
    ) {
        noticeService.deleteNotice(noticeId)
    }

    @PostMapping("/tag")
    fun enrollTag(
        @RequestBody tagName: Map<String, String>
    ) : ResponseEntity<String> {
        noticeService.enrollTag(tagName["name"]!!)
        return ResponseEntity<String>("등록되었습니다. (tagName: ${tagName["name"]})", HttpStatus.OK)
    }
}