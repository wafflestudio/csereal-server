package com.wafflestudio.csereal.common.search

import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 색인을 DB 기준으로 다시 만든다.
 *
 * 평소에는 필요 없다 — 기동할 때 전량 색인하고, 글이 바뀌면 그 글만 따라 바뀐다.
 * E2E 는 매 런 생 SQL 로 DB 를 비우고 다시 심는데, 그 경로는 JPA 리스너를 타지 않아
 * 색인이 옛 데이터를 그대로 들고 있다. 시드가 끝난 뒤 이걸 한 번 부르면 맞춰진다.
 *
 * 프로덕션에는 두지 않는다. 거기서 색인이 꼬이면 재시작이 곧 재색인이다.
 */
@Profile("!prod")
@RequestMapping("/api/v2/search")
@RestController
class SearchIndexController(
    private val searchIndexService: SearchIndexService
) {
    @PostMapping("/reindex")
    fun reindex(): Map<String, Int> =
        searchIndexService.reindexAll().mapKeys { it.key.toValue() }
}
