package com.wafflestudio.csereal.common.search

/**
 * 도메인이 색인에 참여하는 유일한 접점.
 * 검색에 새 도메인을 넣으려면 이걸 구현한 빈을 하나 만들기만 하면 된다 —
 * SearchIndexService 는 구현체 목록을 주입받을 뿐 도메인을 알지 못한다.
 *
 * 한 구현체가 여러 SearchType 을 내도 된다(구성원 = 교수 + 직원).
 */
interface SearchDocumentProvider {
    fun collectAll(): List<SearchDocument>

    /**
     * 글 하나가 바뀌었을 때 그 문서만 다시 만든다. 담당하지 않는 타입이면 null.
     *
     * 기본 구현은 전부 만들어 걸러낸다. 번역본이 있는 타입은 가장 큰 것이 113건(교과목)이라
     * 그래도 싸다. 만 건이 넘는 공지·새소식·세미나만 findById 로 재정의한다.
     */
    fun collectOne(type: SearchType, sourceId: Long): SearchDocument? =
        collectAll().find { it.type == type && it.sourceId == sourceId }
}
