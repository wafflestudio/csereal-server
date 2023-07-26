package com.wafflestudio.csereal.core.notice.api

import com.wafflestudio.csereal.core.notice.dto.CreateNoticeRequest
import com.wafflestudio.csereal.core.notice.dto.NoticeDto
import com.wafflestudio.csereal.core.notice.dto.UpdateNoticeRequest
import com.wafflestudio.csereal.core.notice.service.NoticeService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RequestMapping("/notice")
@RestController
class NoticeController(
    private val noticeService: NoticeService,
) {
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

    @PutMapping("/{noticeId}")
    fun updateNotice(
        @PathVariable noticeId: Long,
        @Valid @RequestBody request: UpdateNoticeRequest,
    ) : NoticeDto {
        return noticeService.updateNotice(noticeId, request)
    }
}