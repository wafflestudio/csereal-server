package com.wafflestudio.csereal.common.search

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Service
class SearchSyncService(
    private val searchIndexService: SearchIndexService,
    private val providers: List<SearchDocumentProvider>
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * AFTER_COMMIT 이라 롤백된 쓰기는 색인되지 않는다. 첨부 파일 삭제가 쓰는 것과 같은 방식이다.
     *
     * 여기서 실패해도 예외를 밖으로 내보내지 않는다 — 커밋은 이미 끝났으니 되돌릴 수 없고,
     * 어긋남은 다음 기동의 전량 재색인이 정리한다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    // AFTER_COMMIT 은 트랜잭션이 이미 끝난 뒤라 REQUIRES_NEW 로 새로 연다.
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    fun onChanged(event: SearchDocumentChanged) {
        if (suspended.get()) return
        runCatching {
            val document = providers.firstNotNullOfOrNull { it.collectOne(event.type, event.sourceId) }
            if (document != null) {
                searchIndexService.indexOne(document)
            } else {
                // 부모가 지워졌는데 번역본 삭제만 먼저 왔거나, 색인 대상이 아니게 된 경우.
                searchIndexService.deleteOne(event.type, event.sourceId)
            }
        }.onFailure { logger.error("색인 동기화 실패 {} {}", event.type, event.sourceId, it) }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onRemoved(event: SearchDocumentRemoved) {
        if (suspended.get()) return
        runCatching { searchIndexService.deleteOne(event.type, event.sourceId) }
            .onFailure { logger.error("색인 삭제 실패 {} {}", event.type, event.sourceId, it) }
    }

    companion object {
        private val suspended = ThreadLocal.withInitial { false }

        /**
         * 이 스레드에서 커밋되는 변경의 행 단위 색인을 끈다. 대량 갱신용 — `indexOne` 은
         * 문서마다 `refresh=wait_for` 로 1초 가까이 기다려 수천 행이면 몇 시간이 걸린다.
         * 끝나면 호출자가 [SearchIndexService.reindexAll] 로 한 번에 맞춘다.
         */
        fun <T> withoutRowSync(block: () -> T): T {
            suspended.set(true)
            try {
                return block()
            } finally {
                suspended.remove()
            }
        }
    }
}
