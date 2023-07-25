package com.wafflestudio.csereal.core.notice.api

import com.wafflestudio.csereal.core.notice.dto.CreateNoticeRequest
import com.wafflestudio.csereal.core.notice.dto.NoticeDto
import com.wafflestudio.csereal.core.notice.service.NoticeService
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
        @RequestBody request: CreateNoticeRequest
    ) : NoticeDto {
        return noticeService.createNotice(request)
    }
}