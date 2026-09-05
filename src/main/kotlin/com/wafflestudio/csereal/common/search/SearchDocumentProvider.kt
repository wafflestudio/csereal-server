package com.wafflestudio.csereal.common.search

/**
 * 도메인마다 하나씩 두어 전량 색인에 참여한다.
 * 검색에 새 도메인을 넣으려면 이걸 구현한 빈을 하나 만들기만 하면 된다 —
 * SearchIndexService 는 구현체 목록을 주입받을 뿐 도메인을 알지 못한다.
 */
interface SearchDocumentProvider {
    val domain: SearchDomain
    fun collectAll(): List<SearchDocument>
}
