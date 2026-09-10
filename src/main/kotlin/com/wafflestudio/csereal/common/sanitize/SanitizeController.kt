package com.wafflestudio.csereal.common.sanitize

import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 에디터가 붙여넣기 순간에 부르는 세탁 창구.
 *
 * [SanitizeEntityListener] 와 **같은 함수**를 불러 에디터에 보이는 것과 저장되는 것이 같아진다.
 * 허용 목록만 내려주면 적용 구현이 두 벌(Kotlin/JS)이 돼 결과가 갈릴 수 있다.
 *
 * UX 장치지 방어선이 아니다 — 강제는 리스너가 한다.
 */
@RestController
@RequestMapping("/api/v2/content")
class SanitizeController(
    private val contentSanitizer: ContentSanitizer
) {
    /**
     * ⚠️ 본문을 [ByteArray] 로 받아 UTF-8 로 직접 읽는다.
     * `String` 으로 받으면 `Content-Type` 에 charset 이 없을 때 Spring 이 HTTP 기본값인
     * ISO-8859-1 로 디코드해 **한글이 깨진다**. 붙여넣기 클라이언트가 charset 을 빠뜨리지
     * 않으리라는 보장이 없다.
     *
     * `required = false` 는 빈 붙여넣기를 400 이 아니라 빈 응답으로 돌려주기 위한 것.
     */
    @PostMapping(
        "/sanitize",
        consumes = ["text/html", "text/plain"],
        produces = ["text/html;charset=UTF-8"]
    )
    @PreAuthorize("hasRole('STAFF')")
    @Operation(
        summary = "본문 HTML 세탁",
        description = "저장 시점과 같은 규칙으로 세탁한 HTML 을 돌려준다. " +
            "에디터가 붙여넣기 직후 불러 사용자가 저장될 모습을 그대로 보게 한다. " +
            "본문은 UTF-8 로 읽는다."
    )
    fun sanitize(
        @RequestBody(required = false) html: ByteArray?
    ): String = contentSanitizer.sanitize(html?.toString(Charsets.UTF_8) ?: "")
}
