package com.wafflestudio.csereal.common.search

import jakarta.persistence.PostPersist
import jakarta.persistence.PostRemove
import jakarta.persistence.PostUpdate
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * BaseTimeEntity 에 달려 있어 모든 엔티티의 저장·삭제를 받는다.
 * 색인 대상은 SearchIndexed 를 구현한 것뿐이라 나머지는 지나친다 —
 * 엔티티마다 애너테이션을 달지 않아도 되고, 새 엔티티가 생겨도 빠뜨릴 일이 없다.
 */
@Component
class SearchSyncEntityListener(
    private val eventPublisher: ApplicationEventPublisher
) {
    @PostPersist
    @PostUpdate
    fun onChange(entity: Any) {
        if (entity !is SearchIndexed) return
        eventPublisher.publishEvent(SearchDocumentChanged(entity.searchType, entity.searchSourceId))
    }

    @PostRemove
    fun onRemove(entity: Any) {
        if (entity !is SearchIndexed) return
        eventPublisher.publishEvent(SearchDocumentRemoved(entity.searchType, entity.searchSourceId))
    }
}
