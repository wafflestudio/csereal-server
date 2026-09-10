package com.wafflestudio.csereal.common.sanitize

import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import org.springframework.stereotype.Component

/**
 * 저장 직전에 본문을 세탁한다. **여기가 강제 지점이다** —
 * 에디터의 `/sanitize` 호출은 사용자가 결과를 미리 보게 하는 UX 장치일 뿐 우회 가능하다.
 *
 * `BaseTimeEntity` 에 달려 모든 저장을 받고 [HtmlContentHolder] 만 처리한다
 * (`SearchSyncEntityListener` 와 같은 방식).
 */
@Component
class SanitizeEntityListener(
    private val sanitizer: ContentSanitizer
) {
    @PrePersist
    @PreUpdate
    fun sanitize(entity: Any) {
        if (entity !is HtmlContentHolder) return

        for (field in entity.htmlFields()) {
            val original = field.get() ?: continue
            val cleaned = sanitizer.sanitize(original)
            // 안 바뀌었으면 대입하지 않는다 — 더티 체킹에 없는 변경을 만들지 않는다.
            if (cleaned != original) field.set(cleaned)
        }
    }
}
