package com.wafflestudio.csereal.common.search

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import kotlin.system.measureTimeMillis

@Component
class SearchIndexInitializer(
    private val searchIndexService: SearchIndexService,
    @Value("\${csereal.search.reindex-on-startup}") private val enabled: Boolean
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * ⚠️ 여기서 나는 예외를 절대 밖으로 내보내지 않는다. 검색이 없어도 사이트는 떠야 한다.
     * 예전에 로컬·dev 프로파일의 OIDC 등록 때문에 기동 시 외부 호출이 생겨 학외에서
     * 크래시 루프가 됐다(csereal-server#418). 같은 모양을 다시 만들지 않는다.
     */
    @EventListener(ApplicationReadyEvent::class)
    fun indexOnStartup() {
        if (!enabled) return

        runCatching {
            val counts: Map<SearchDomain, Int>
            val elapsed = measureTimeMillis { counts = searchIndexService.reindexAll() }
            logger.info(
                "검색 색인 완료 {}ms, 총 {}건 {} (ES {})",
                elapsed,
                counts.values.sum(),
                counts.entries.joinToString { "${it.key.toValue()}=${it.value}" },
                searchIndexService.serverVersion()
            )
        }.onFailure {
            logger.error("검색 색인 실패 — 검색만 동작하지 않는다. 나머지는 정상.", it)
        }
    }
}
