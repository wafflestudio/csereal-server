package com.wafflestudio.csereal.common.search

/** JPA 가 저장·삭제를 알린 시점에 발행된다. 실제 색인은 커밋 뒤에 한다. */
data class SearchDocumentChanged(val type: SearchType, val sourceId: Long)

data class SearchDocumentRemoved(val type: SearchType, val sourceId: Long)
