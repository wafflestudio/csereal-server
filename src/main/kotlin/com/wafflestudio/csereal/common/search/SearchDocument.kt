package com.wafflestudio.csereal.common.search

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class SearchDomain {
    NOTICE, NEWS, SEMINAR, ABOUT, ACADEMICS, MEMBER, RESEARCH, ADMISSIONS;

    @JsonValue
    fun toValue() = name.lowercase()
}

/**
 * 한 부모(원본 글) = 문서 하나. 번역본은 언어별 필드로 담는다.
 *
 * 언어로 문서를 나누지 않는 이유: 한국어 화면에서 영문명("Kang")으로 찾는 일이 흔한데
 * 실측상 교수 이름은 한/영이 글자로 하나도 겹치지 않는다(강유 ↔ U Kang).
 * 네 필드를 한꺼번에 검색하면 어느 언어로 쳐도 걸리고, 문서가 하나라 결과 중복도 없다.
 * 화면에 무엇을 그릴지는 프론트가 화면 언어로 고른다.
 */
data class SearchDocument(
    val domain: SearchDomain,
    val sourceId: Long,
    val titleKo: String?,
    val titleEn: String?,
    val bodyKo: String?,
    val bodyEn: String?,
    val createdAt: String?,
    // Kotlin 의 is 접두사 프로퍼티는 게터가 isPrivate() 라 Jackson 이 이름을 private 으로 줄인다.
    // 매핑에 없는 이름이 되어 strict 인덱스가 거절한다.
    @get:JsonProperty("isPrivate")
    val isPrivate: Boolean
) {
    // 재색인이 삽입이 아니라 덮어쓰기가 되도록 id 를 내용에서 만든다.
    // getter 모양이 아니라서 문서 본문(dynamic: strict)에는 실리지 않는다.
    fun documentId() = "${domain.toValue()}:$sourceId"

    companion object {
        fun timestamp(value: LocalDateTime?): String? =
            value?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}
