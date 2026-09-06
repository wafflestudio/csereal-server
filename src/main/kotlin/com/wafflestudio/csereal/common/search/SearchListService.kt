package com.wafflestudio.csereal.common.search

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.SortOrder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * 게시판 목록(공지·새소식·세미나)의 키워드 검색.
 *
 * ES 는 "어떤 글이 몇 번째로 오느냐"만 정한다. 화면에 그릴 값은 부르는 쪽이 id 로
 * DB 에서 읽는다 — 목록마다 필요한 컬럼이 달라서 색인에 담기 시작하면 끝이 없고,
 * 그 값이 바뀔 때마다 재색인해야 한다.
 */
@Service
class SearchListService(
    private val client: ElasticsearchClient,
    @Value("\${csereal.search.index}") private val indexName: String
) {
    fun searchIds(
        type: SearchType,
        keyword: String,
        tags: List<String>,
        isStaff: Boolean,
        offset: Long,
        size: Int
    ): IdPage {
        val response = client.search(
            { request ->
                request.index(indexName)
                    .from(offset.toInt())
                    .size(size)
                    // 목록에 필요한 건 id 뿐이다. 본문까지 실어 오면 한 페이지에 수십 KB 가 된다.
                    .source { it.filter { f -> f.includes("sourceId") } }
                    .query { query ->
                        query.bool { bool ->
                            bool.must { must -> must.multiMatch { it.query(keyword).fields(SEARCH_FIELDS) } }
                            bool.filter { f -> f.term { it.field("type").value(type.toValue()) } }
                            if (!isStaff) {
                                bool.filter { f -> f.term { it.field("isPrivate").value(false) } }
                            }
                            if (tags.isNotEmpty()) {
                                bool.filter { f ->
                                    f.terms { t ->
                                        t.field("tags").terms { v -> v.value(tags.map(FieldValue::of)) }
                                    }
                                }
                            }
                            bool
                        }
                    }
                    // 고정글이 맨 위, 그다음 최신순. 고정 개념이 없는 목록에선 앞 기준이 모두 같아 무시된다.
                    .sort { s -> s.field { it.field("isPinned").order(SortOrder.Desc) } }
                    .sort { s -> s.field { it.field("createdAt").order(SortOrder.Desc) } }
            },
            SourceId::class.java
        )
        return IdPage(
            total = response.hits().total()?.value() ?: 0,
            ids = response.hits().hits().mapNotNull { it.source()?.sourceId }
        )
    }

    companion object {
        private val SEARCH_FIELDS = listOf("titleKo^3", "titleEn^3", "bodyKo", "bodyEn")
    }
}

data class IdPage(val total: Long, val ids: List<Long>)

/** 위 source 필터로 걸러낸 문서. 다른 필드는 오지 않는다. */
data class SourceId(val sourceId: Long)
