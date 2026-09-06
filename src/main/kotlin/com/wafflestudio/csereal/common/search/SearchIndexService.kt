package com.wafflestudio.csereal.common.search

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.Refresh
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SearchIndexService(
    private val client: ElasticsearchClient,
    private val providers: List<SearchDocumentProvider>,
    @Value("\${csereal.search.index}") private val indexName: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 인덱스를 지우고 매핑과 함께 새로 만든 뒤 전 도메인을 다시 넣는다.
     *
     * 갱신이 아니라 재생성인 이유: Flyway 마이그레이션이 JPA 밖에서 색인 대상 테이블을 고치므로
     * (V21 의 `UPDATE member_search` 등) 이벤트만으로는 어긋남을 따라잡지 못한다.
     * 매번 새로 만드는 덕에 매핑·분석기 변경도 재시작만으로 반영된다.
     */
    @Transactional(readOnly = true)
    fun reindexAll(): Map<SearchType, Int> {
        recreateIndex()
        val counts = mutableMapOf<SearchType, Int>()
        // provider 단위로 넣는다 — 16,000건을 한꺼번에 메모리에 올리지 않기 위해.
        providers.forEach { provider ->
            val documents = provider.collectAll()
            index(documents)
            documents.forEach { counts.merge(it.type, 1, Int::plus) }
        }
        return counts
    }

    fun serverVersion(): String = client.info().version().number()

    // refresh=wait_for — 넣자마자 검색되게 한다. ES 는 기본 1초 지연이 있어
    // 글을 저장하고 바로 검색하면 안 나온다(시드 직후 검증하는 E2E 가 그 경우다).
    fun indexOne(document: SearchDocument) {
        client.index { request ->
            request.index(indexName)
                .id(document.documentId())
                .document(document)
                .refresh(Refresh.WaitFor)
        }
    }

    fun deleteOne(type: SearchType, sourceId: Long) {
        client.delete { request ->
            request.index(indexName)
                .id("${'$'}{type.toValue()}:${'$'}sourceId")
                .refresh(Refresh.WaitFor)
        }
    }

    private fun recreateIndex() {
        client.indices().delete { it.index(indexName).ignoreUnavailable(true) }
        ClassPathResource("search/index-settings.json").inputStream.reader().use { reader ->
            client.indices().create(
                CreateIndexRequest.Builder().index(indexName).withJson(reader).build()
            )
        }
    }

    // 한 번에 다 보내면 요청이 커져 거절당한다. 나눠 보내고 실패는 건별로 남긴다.
    private fun index(documents: List<SearchDocument>): Int {
        documents.chunked(BULK_CHUNK).forEach { chunk ->
            val response = client.bulk { bulk ->
                bulk.index(indexName)
                chunk.forEach { document ->
                    bulk.operations { operation ->
                        operation.index { it.id(document.documentId()).document(document) }
                    }
                }
                bulk
            }
            if (response.errors()) {
                response.items().filter { it.error() != null }.forEach {
                    logger.error("색인 실패 id={} reason={}", it.id(), it.error()?.reason())
                }
            }
        }
        return documents.size
    }

    companion object {
        private const val BULK_CHUNK = 2000
    }
}
