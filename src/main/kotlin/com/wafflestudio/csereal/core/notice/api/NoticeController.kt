package com.wafflestudio.csereal.core.notice.api

import com.wafflestudio.csereal.core.notice.dto.CreateNoticeRequest
import com.wafflestudio.csereal.core.notice.dto.NoticeDto
import com.wafflestudio.csereal.core.notice.service.NoticeService
import org.springframework.web.bind.annotation.*

@RequestMapping
@RestController
class NoticeController(
    private val noticeService: NoticeService,
) {
    @GetMapping("/node/{id}")
    fun readNotice(
        @PathVariable id: Long,
    ) : NoticeDto {
        return noticeService.readNotice(id)
    }

    @PostMapping("/node")
    fun createNotice(
        @RequestBody request: CreateNoticeRequest
    ) : NoticeDto {
        return noticeService.createNotice(request)
    }
}