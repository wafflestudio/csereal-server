package com.wafflestudio.csereal.common.search

/** 이 엔티티가 바뀌면 어떤 검색 문서를 다시 만들어야 하는지 스스로 안다. */
interface SearchIndexed {
    val searchType: SearchType

    /** 문서가 가리킬 원본 id. 번역본이면 부모의 id 다. */
    val searchSourceId: Long
}
