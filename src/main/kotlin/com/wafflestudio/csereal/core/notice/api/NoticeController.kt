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
    @GetMapping("/search")
    fun searchNotice(
        @RequestBody request: SearchRequest
    ): List<SearchResponse> {
        return noticeService.searchNotice(request.tags, request.keyWord)
    }
    @GetMapping("/{noticeId}")
    fun readNotice(
        @PathVariable noticeId: Long,
    ) : NoticeDto {
        return noticeService.readNotice(noticeId)
    }

    @PostMapping
    fun createNotice(
        @Valid @RequestBody request: CreateNoticeRequest
    ) : NoticeDto {
        return noticeService.createNotice(request)
    }

    @PatchMapping("/{noticeId}")
    fun updateNotice(
        @PathVariable noticeId: Long,
        @Valid @RequestBody request: UpdateNoticeRequest,
    ) : NoticeDto {
        return noticeService.updateNotice(noticeId, request)
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