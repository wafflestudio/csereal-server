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
}
