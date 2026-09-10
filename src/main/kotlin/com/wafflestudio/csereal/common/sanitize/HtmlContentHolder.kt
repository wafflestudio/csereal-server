package com.wafflestudio.csereal.common.sanitize

/**
 * 저장 전에 세탁할 HTML 본문을 가진 엔티티.
 *
 * `SearchIndexed` 와 같은 방식이다 — 리스너가 `BaseTimeEntity` 에 붙어 모든 저장을 받고
 * 이걸 구현한 것만 처리하므로, 새 엔티티를 빠뜨릴 수 없다.
 */
interface HtmlContentHolder {
    fun htmlFields(): List<HtmlField>
}

/**
 * 세탁 대상 필드 하나. getter·setter 쌍으로 받는다.
 *
 * 프로퍼티 참조(`::description`)를 그대로 받으면 더 짧지만, 본문이 `String` 인 엔티티와
 * `String?` 인 엔티티가 섞여 있어 한 타입으로 못 묶는다.
 */
class HtmlField(val get: () -> String?, val set: (String) -> Unit)
