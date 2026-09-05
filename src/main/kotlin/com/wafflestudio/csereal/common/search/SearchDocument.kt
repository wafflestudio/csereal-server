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
    /**
     * 목록에 보이고 정렬 기준이 되는 날짜. 글이 만들어진 시각과는 다를 수 있다 —
     * 새소식은 date, 세미나는 startDate 가 그 자리를 대신한다.
     */
    val createdAt: String?,
    /**
     * 이 문서가 사는 화면의 경로(로케일 프리픽스 없이). 프론트가 그대로 링크에 쓴다.
     *
     * type + id 만으로는 못 만드는 타입이 있다 — /about/{postType},
     * /academics/{studentType}/{postType} 처럼 백엔드 enum 이 경로를 정하기 때문이다.
     * 그 곱집합을 SearchType 으로 베끼면 enum 5개를 두 벌로 들고 있게 되므로,
     * provider 가 기존 enum 에서 조립해 문서에 담는다.
     */
    val url: String,
    /**
     * 목록에 띄울 사진(새소식·교수·연구실 등). 절대 URL 이라 환경마다 다르지만
     * 기동할 때마다 재색인하므로 항상 그 환경의 값이다.
     */
    val thumbnailUrl: String?,
    // Kotlin 의 is 접두사 프로퍼티는 게터가 isPrivate() 라 Jackson 이 이름을 private 으로 줄인다.
    // 매핑에 없는 이름이 되어 strict 인덱스가 거절한다.
    @get:JsonProperty("isPrivate")
    val isPrivate: Boolean = false,
    /**
     * 게시판 목록의 태그 필터용. 표시에는 안 쓴다(칩 라벨은 프론트가 만든다).
     * 공지·새소식만 태그가 있고 나머지는 빈 목록이다.
     */
    val tags: List<String> = emptyList(),
    // 공지 목록은 고정글을 맨 위에 올린다. 정렬을 ES 가 하므로 그 값도 여기 있어야 한다.
    @get:JsonProperty("isPinned")
    val isPinned: Boolean = false
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
            url: (ROW) -> String,
            thumbnailUrl: (ROW) -> String? = { null },
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
                url = url(representative),
                thumbnailUrl = thumbnailUrl(representative),
                createdAt = timestamp(createdAt(representative))
            )
        }
    }
}
