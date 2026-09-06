package com.wafflestudio.csereal.core.search.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.Time
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreQuery
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
                    // 한 화면으로 가는 결과는 한 줄로 묶는다. 학회 87개가 학회 목록 한 장을,
                    // 학사 안내의 연도별 행이 같은 페이지를 가리켜 상위를 도배했다.
                    .collapse { it.field("url") }
                    // collapse 하면 hits.total 은 묶기 전 문서 수라 화면에 쓸 수 없다.
                    .aggregations(PAGE_COUNT) { agg ->
                        agg.cardinality { it.field("url").precisionThreshold(MAX_PRECISION) }
                    }
                    .query { query -> query.functionScore { fs -> relevance(fs, keyword, types) } }
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
            total = response.aggregations()[PAGE_COUNT]?.cardinality()?.value() ?: 0,
            results = response.hits().hits().mapNotNull { toElement(it, language) }
        )
    }

    /**
     * 관련도(BM25)에 두 가지 사전 지식을 곱한다.
     *
     * 배수 = 1 + (구조 페이지면 0.8) + (최신이면 최대 0.8)
     *
     * 감점이 아니라 가산점인 게 중요하다. 오래되면 깎는 방식으로 짰더니 2011년 기사를
     * 제목째 검색해도 안 나왔다. 이 방식은 옛 문서의 점수를 그대로 두고 최신 문서만 올린다.
     * 날짜가 없는 구조 페이지는 감쇠 함수가 건너뛰어 불이익이 없다.
     */
    private fun relevance(
        fs: FunctionScoreQuery.Builder,
        keyword: String,
        types: List<SearchType>?
    ) = fs.query { query ->
        query.bool { bool ->
            bool.must { must -> must.multiMatch { it.query(keyword).fields(SEARCH_FIELDS) } }
            // 비공개 글은 교직원에게만 보인다.
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
    }.functions(
        FunctionScore.of { fn -> fn.weight(1.0) },
        FunctionScore.of { fn ->
            fn.filter { f ->
                f.terms { t ->
                    t.field("type").terms { v ->
                        v.value(STANDING_TYPES.map { FieldValue.of(it.toValue()) })
                    }
                }
            }.weight(STANDING_BONUS)
        },
        FunctionScore.of { fn ->
            fn.gauss { decay ->
                decay.date { date ->
                    date.field("createdAt").placement { p ->
                        p.origin("now")
                            .scale(Time.of { it.time(RECENCY_SCALE) })
                            .offset(Time.of { it.time(RECENCY_OFFSET) })
                            .decay(0.5)
                    }
                }
            }.weight(RECENCY_BONUS)
        }
    ).scoreMode(FunctionScoreMode.Sum).boostMode(FunctionBoostMode.Multiply)

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

        private const val PAGE_COUNT = "pages"

        // 서로 다른 url 이 1.6만 개를 넘지 않아 이 값이면 집계가 사실상 정확하다(실측 오차 0.02%).
        private const val MAX_PRECISION = 40000

        /**
         * 날짜가 없는 상시 페이지. 공지가 전체의 90% 라 이 가산점이 없으면
         * "대학원 입학" 을 쳐도 대학원 안내 페이지가 상위에 못 온다.
         */
        private val STANDING_TYPES = listOf(
            SearchType.ABOUT, SearchType.ADMISSIONS, SearchType.ACADEMICS, SearchType.COURSE,
            SearchType.SCHOLARSHIP, SearchType.PROFESSOR, SearchType.EMERITUS_PROFESSOR,
            SearchType.STAFF, SearchType.LAB, SearchType.RESEARCH_GROUP,
            SearchType.RESEARCH_CENTER, SearchType.CONFERENCE
        )
        private const val STANDING_BONUS = 0.8
        private const val RECENCY_BONUS = 0.8

        // 1년 안쪽은 가산점을 다 받고, 그 뒤 4년에 걸쳐 절반으로 준다.
        private const val RECENCY_SCALE = "1460d"
        private const val RECENCY_OFFSET = "365d"
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
