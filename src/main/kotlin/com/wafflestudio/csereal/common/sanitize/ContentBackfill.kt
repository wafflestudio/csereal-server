package com.wafflestudio.csereal.common.sanitize

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.search.SearchIndexService
import com.wafflestudio.csereal.common.search.SearchSyncService
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * 리스너가 붙기 전에 저장된 본문을 한 번 세탁한다.
 *
 * [HtmlContentHolder] 엔티티를 500건씩 읽어 본문을 세탁 결과로 덮어쓴다. 값이 바뀐 것만 dirty 가
 * 되고, 평문 파생은 setter 가 한다. 검색 색인은 행 단위 동기화를 끄고 끝에 전량 재색인한다 —
 * 행마다 하면 refresh 대기 때문에 몇 시간이 걸린다. 세탁은 멱등이라 중간에 끊겨도 다시 돌리면 된다.
 */
@Component
class ContentBackfill(
    private val sanitizer: ContentSanitizer,
    private val em: EntityManager,
    private val searchIndexService: SearchIndexService,
    transactionManager: PlatformTransactionManager
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val tx = TransactionTemplate(transactionManager)

    /** 엔티티별로 바뀐 행 수. */
    fun run(chunk: Int = 500): Map<String, Int> {
        val changed = SearchSyncService.withoutRowSync { sanitizeAll(chunk) }
        // 실패해도 본문은 이미 고쳐졌다. 색인은 다음 기동이 다시 만든다.
        runCatching { searchIndexService.reindexAll() }
            .onSuccess { log.info("backfill 재색인 {}", it) }
            .onFailure { log.error("backfill 재색인 실패 — 다음 기동의 전량 재색인에 맡긴다", it) }
        return changed
    }

    private fun sanitizeAll(chunk: Int): Map<String, Int> {
        val targets = em.metamodel.entities.filter { HtmlContentHolder::class.java.isAssignableFrom(it.javaType) }
        return targets.associate { type ->
            var cursor = 0L
            var changed = 0
            do {
                // 청크마다 트랜잭션을 끊어 flush 가 쌓이지 않게 한다.
                val n = tx.execute {
                    // FOR UPDATE — 이 청크를 쓰는 사이 들어온 편집은 커밋을 기다렸다가 그 위에 덧쓴다.
                    // 편집이 이긴다. 방문자의 읽기는 스냅샷이라 락에 안 걸린다.
                    val rows = em.createQuery(
                        "SELECT e FROM ${type.name} e WHERE e.id > :cursor ORDER BY e.id",
                        type.javaType
                    ).setParameter("cursor", cursor).setMaxResults(chunk)
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE).resultList
                    for (row in rows) {
                        cursor = (row as BaseTimeEntity).id
                        if (sanitize(row as HtmlContentHolder)) changed++
                    }
                    rows.size
                }!!
            } while (n == chunk)
            log.info("backfill {} — 바뀐 행 {}", type.name, changed)
            type.name to changed
        }
    }

    private fun sanitize(holder: HtmlContentHolder): Boolean {
        var dirty = false
        for (field in holder.htmlFields()) {
            val before = field.get() ?: continue
            val after = sanitizer.sanitize(before)
            if (after != before) {
                field.set(after)
                dirty = true
            }
        }
        return dirty
    }
}
