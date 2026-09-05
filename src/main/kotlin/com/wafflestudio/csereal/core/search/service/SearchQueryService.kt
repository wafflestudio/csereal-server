package com.wafflestudio.csereal.core.search.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch.core.search.HighlighterType
import co.elastic.clients.elasticsearch.core.search.Hit
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchType
import com.wafflestudio.csereal.common.utils.isCurrentUserStaff
import com.wafflestudio.csereal.core.search.api.res.PreviewSegment
import com.wafflestudio.csereal.core.search.api.res.SearchResBody
import com.wafflestudio.csereal.core.search.api.res.SearchResElement
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SearchQueryService(
    private val client: ElasticsearchClient,
    @Value("\${csereal.search.index}") private val indexName: String
) {
    fun search(
        keyword: String,
        language: LanguageType,
        types: List<SearchType>?,
        pageNum: Int,
        pageSize: Int
    ): SearchResBody {
        val response = client.search(
            { request ->
                request.index(indexName)
                    .from((pageNum - 1) * pageSize)
                    .size(pageSize)
                    .query { query ->
                        query.bool { bool ->
                            bool.must { must ->
                                must.multiMatch { it.query(keyword).fields(SEARCH_FIELDS) }
                            }
                            // 비공개 글은 교직원에게만 보인다. 지금 MySQL 검색과 같은 규칙이다.
                            if (!isCurrentUserStaff()) {
                                bool.filter { f -> f.term { it.field("isPrivate").value(false) } }
                            }
                            types?.takeIf { it.isNotEmpty() }?.let { list ->
                                bool.filter { f ->
                                    f.terms { t ->
                                        t.field("type").terms { v ->
                                            v.value(list.map { type -> FieldValue.of(type.toValue()) })
                                        }
                                    }
                                }
                            }
                            bool
                        }
                    }
                    .highlight { hl ->
                        // 표시자로 제어문자를 쓴다. <em> 같은 태그를 쓰면 원문에 같은 글자가
                        // 있을 때 조각 나누기가 어긋난다.
                        hl.preTags(HIT_OPEN).postTags(HIT_CLOSE)
                            // 기본 하이라이터는 문장 경계에서 끊어, 검색어가 든 문장이 짧으면
                            // fragmentSize 와 무관하게 그 문장만 돌려준다(실측 평균 118자).
                            // plain 은 문장을 보지 않고 창을 채운다.
                            .type(HighlighterType.Plain)
                            .fragmentSize(FRAGMENT_SIZE)
                            .numberOfFragments(1)
                        HIGHLIGHT_FIELDS.forEach { field -> hl.fields(field) { it } }
                        hl
                    }
            },
            SearchDocument::class.java
        )
        return SearchResBody(
            total = response.hits().total()?.value() ?: 0,
            results = response.hits().hits().mapNotNull { toElement(it, language) }
        )
    }

    private fun toElement(hit: Hit<SearchDocument>, language: LanguageType): SearchResElement? {
        val document = hit.source() ?: return null
        val korean = language == LanguageType.KO
        val title = (if (korean) document.titleKo else document.titleEn)
            ?: document.titleKo ?: document.titleEn ?: return null

        return SearchResElement(
            type = document.type,
            id = document.sourceId,
            title = title,
            url = document.url,
            thumbnailUrl = document.thumbnailUrl,
            date = document.createdAt,
            preview = preview(hit, document, korean)
        )
    }

    /**
     * 하이라이트가 온 필드를 그대로 미리보기로 쓴다. 화면 언어를 먼저 보되, 다른 언어에서
     * 맞았으면 그쪽을 보여준다 — "왜 이게 걸렸는지"가 보이는 편이 낫다.
     * 본문에서 아무것도 안 맞았으면(제목만 맞은 경우) 화면 언어 본문의 앞부분을 준다.
     */
    private fun preview(
        hit: Hit<SearchDocument>,
        document: SearchDocument,
        korean: Boolean
    ): List<PreviewSegment> {
        val preferred = if (korean) listOf("bodyKo", "bodyEn") else listOf("bodyEn", "bodyKo")
        val fragment = preferred.firstNotNullOfOrNull { hit.highlight()[it]?.firstOrNull() }
        if (fragment != null) return toSegments(fragment)

        val body = (if (korean) document.bodyKo else document.bodyEn)
            ?: document.bodyKo ?: return emptyList()
        return listOf(PreviewSegment(body.trim().take(FRAGMENT_SIZE), hit = false))
    }

    private fun toSegments(fragment: String): List<PreviewSegment> {
        val segments = fragment.split(HIT_OPEN).flatMapIndexed { index, chunk ->
            if (index == 0) {
                listOf(PreviewSegment(chunk, hit = false))
            } else {
                val parts = chunk.split(HIT_CLOSE, limit = 2)
                listOf(
                    PreviewSegment(parts[0], hit = true),
                    PreviewSegment(parts.getOrElse(1) { "" }, hit = false)
                )
            }
        }.filter { it.text.isNotEmpty() }

        // 본문에 개행·들여쓰기가 그대로 들어 있어 조각이 공백으로 시작·끝나는 일이 잦다.
        return segments.mapIndexed { index, segment ->
            var text = segment.text
            if (index == 0) text = text.trimStart()
            if (index == segments.lastIndex) text = text.trimEnd()
            segment.copy(text = text)
        }.filter { it.text.isNotEmpty() }
    }

    companion object {
        private val SEARCH_FIELDS = listOf("titleKo^3", "titleEn^3", "bodyKo", "bodyEn")
        private val HIGHLIGHT_FIELDS = listOf("bodyKo", "bodyEn")
        private const val HIT_OPEN = "\u0002"
        private const val HIT_CLOSE = "\u0003"

        /**
         * 프론트는 2줄(약 170자)만 보여주고 나머지는 자른다. 넉넉히 넘겨 두 줄이 늘 차게 한다.
         * 무작정 키우면 안 된다 — 창이 커질수록 강조가 조각 뒤로 밀려 잘린 부분에 들어간다.
         * 실측(검색어 10개 × 상위 20건): 240 은 강조가 안 보이는 게 4/184, 300 부터 13/184.
         */
        private const val FRAGMENT_SIZE = 240
    }
}
