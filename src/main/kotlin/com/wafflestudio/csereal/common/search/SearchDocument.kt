package com.wafflestudio.csereal.common.search

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.csereal.common.enums.LanguageType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 한 부모(원본 글) = 문서 하나. 번역본은 언어별 필드로 담는다.
 *
 * 언어로 문서를 나누지 않는 이유: 한국어 화면에서 영문명("Kang")으로 찾는 일이 흔한데
 * 실측상 교수 이름은 한/영이 글자로 하나도 겹치지 않는다(강유 ↔ U Kang).
 * 네 필드를 한꺼번에 검색하면 어느 언어로 쳐도 걸리고, 문서가 하나라 결과 중복도 없다.
 *
 * 화면 표시용 값(사진 URL·직급·학생 구분 등)은 넣지 않는다. 검색 결과를 만들 때
 * sourceId 로 DB 에서 가져온다 — 그 값이 바뀌었다고 재색인할 이유를 만들지 않는다.
 */
data class SearchDocument(
    val type: SearchType,
    val sourceId: Long,
    val titleKo: String?,
    val titleEn: String?,
    val bodyKo: String?,
    val bodyEn: String?,
    val createdAt: String?,
    // Kotlin 의 is 접두사 프로퍼티는 게터가 isPrivate() 라 Jackson 이 이름을 private 으로 줄인다.
    // 매핑에 없는 이름이 되어 strict 인덱스가 거절한다.
    @get:JsonProperty("isPrivate")
    val isPrivate: Boolean = false
) {
    // 재색인이 삽입이 아니라 덮어쓰기가 되도록 id 를 내용에서 만든다.
    // getter 모양이 아니라서 문서 본문(dynamic: strict)에는 실리지 않는다.
    fun documentId() = "${type.toValue()}:$sourceId"

    companion object {
        fun timestamp(value: LocalDateTime?): String? =
            value?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        /**
         * 번역본 행들을 하나의 문서로 묶는다. *_search 테이블은 번역본마다 한 행이라
         * 그대로 색인하면 한 사람이 문서 둘이 된다.
         *
         * groupBy 가 "같은 것"의 기준이다. V21·V22 로 부모가 생긴 도메인은 부모 id 를,
         * 부모가 없는 도메인(교과목·학회·입학)은 자연키를 준다.
         */
        fun <ROW, KEY> bilingual(
            rows: List<ROW>,
            type: SearchType,
            groupBy: (ROW) -> KEY,
            sourceId: (ROW) -> Long,
            language: (ROW) -> LanguageType,
            title: (ROW) -> String?,
            body: (ROW) -> String?,
            createdAt: (ROW) -> LocalDateTime? = { null }
        ): List<SearchDocument> = rows.groupBy(groupBy).map { (_, group) ->
            val ko = group.find { language(it) == LanguageType.KO }
            val en = group.find { language(it) == LanguageType.EN }
            val representative = ko ?: en!!
            SearchDocument(
                type = type,
                sourceId = sourceId(representative),
                titleKo = ko?.let(title),
                titleEn = en?.let(title),
                bodyKo = ko?.let(body),
                bodyEn = en?.let(body),
                createdAt = timestamp(createdAt(representative))
            )
        }
    }
}
