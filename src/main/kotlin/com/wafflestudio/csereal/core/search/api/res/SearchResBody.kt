package com.wafflestudio.csereal.core.search.api.res

import com.wafflestudio.csereal.common.search.SearchType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "검색 결과. 도메인 구분 없이 관련도 순으로 한 줄로 준다.")
data class SearchResBody(
    val total: Long,
    val results: List<SearchResElement>
)

data class SearchResElement(
    @Schema(description = "결과의 종류. 행 모양·배지·링크 주소가 여기 따라 달라진다.")
    val type: SearchType,
    @Schema(description = "원본 글의 id. type 안에서만 유일하다.")
    val id: Long,
    @Schema(description = "요청한 language 의 제목. 그 언어판이 없으면 다른 쪽을 준다.")
    val title: String,
    @Schema(description = "이 결과가 사는 화면 경로(로케일 프리픽스 없음). 그대로 링크에 쓴다.")
    val url: String,
    @Schema(description = "본문 미리보기. hit=true 인 조각이 검색어와 맞은 부분이다.")
    val preview: List<PreviewSegment>
)

@Schema(description = "미리보기 조각. 이어 붙이면 원래 문장이 된다.")
data class PreviewSegment(
    val text: String,
    val hit: Boolean
)
