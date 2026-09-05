package com.wafflestudio.csereal.common.search

import co.elastic.clients.elasticsearch.ElasticsearchClient
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
    fun reindexAll(): Map<SearchDomain, Int> {
        recreateIndex()
        return providers.associate { provider ->
            provider.domain to index(provider.collectAll())
        }
    }

    fun serverVersion(): String = client.info().version().number()

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
