package com.wafflestudio.csereal.common.sanitize

import com.wafflestudio.csereal.common.interceptor.InternalOnly
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/content")
class ContentBackfillController(
    private val backfill: ContentBackfill
) {
    /** 컨테이너 안에서만 부른다 — `docker exec <backend> curl -X POST localhost:8080/api/v2/content/backfill`. */
    @InternalOnly
    @PostMapping("/backfill")
    @Operation(summary = "기존 본문 일괄 세탁", description = "리스너가 붙기 전에 저장된 본문을 저장 시점과 같은 규칙으로 세탁한다. loopback 전용.")
    fun backfill(): Map<String, Int> = backfill.run()
}
