package com.wafflestudio.csereal.common.sanitize

import org.owasp.html.HtmlPolicyBuilder
import org.owasp.html.PolicyFactory

/**
 * 본문 HTML 에서 살릴 태그·속성.
 *
 * 목록은 운영 덤프(본문 201MB)의 태그·속성 census 에서 나왔다. 실제로 쓰이는 것만 담고,
 * 실행 가능한 것(`script`·`iframe`·`form`·`input`·`button`)은 전부 뺀다.
 *
 * 태그·속성 집합은 HTML 명세가 정하는 **닫힌** 집합이라 출처가 늘어도 늘지 않는다.
 * 워드가 새 버전을 내도 `<script>` 를 만들지 않고, 브라우저 확장이 무엇을 심어도
 * 실행 가능한 요소의 종류는 그대로다. 열려 있는 쪽은 CSS 인데 그건 [ContentCssSchema] 가 맡는다.
 */
object ContentPolicy {

    /** 코퍼스에 실제로 등장하고 렌더에 의미가 있는 것만. 출현 순. */
    private val ELEMENTS = arrayOf(
        "span", "p", "td", "strong", "br", "div", "tr", "a", "li", "tbody", "table",
        "img", "ul", "u", "figure", "col", "ol", "em", "th", "colgroup", "hr",
        "h1", "h2", "h3", "h4", "h5", "h6", "b", "pre", "blockquote", "sub", "sup",
        "dl", "dt", "dd", "thead", "tfoot", "code", "address", "s", "caption", "i"
    )

    /**
     * suneditor 가 이미지를 다시 편집할 때 읽는 값들. 지우면 에디터에서 크기·정렬 조절이 깨진다.
     * (`data-size`·`data-align` 등은 플러그인이 DOM 에서 되읽는다.)
     */
    private val IMAGE_DATA_ATTRIBUTES = arrayOf(
        "data-size", "data-proportion", "data-origin", "data-align",
        "data-rotate", "data-percentage", "data-file-name", "data-file-size",
        "data-image-link"
    )

    val INSTANCE: PolicyFactory = HtmlPolicyBuilder()
        .allowElements(*ELEMENTS)
        .allowAttributes("class", "id", "title", "lang", "dir").globally()
        .allowAttributes("href", "target", "rel").onElements("a")
        .allowAttributes("src", "alt", "width", "height", "align", *IMAGE_DATA_ATTRIBUTES)
        .onElements("img", "figure")
        .allowAttributes("colspan", "rowspan", "valign", "align", "width", "height", "nowrap", "bgcolor")
        .onElements("td", "th", "tr", "table", "col", "colgroup")
        .allowAttributes("cellpadding", "cellspacing", "border", "summary").onElements("table")
        .allowAttributes("start", "type").onElements("ol")
        // http·https·mailto·tel 만. data: 는 열지 않는다 — 프로토콜 단위로는
        // data:image/png 와 <script> 를 품은 data:image/svg+xml 을 못 가른다.
        // 본문의 data:image 는 세탁 **전에** 파일로 추출한다(ContentSanitizer).
        .allowStandardUrlProtocols()
        .allowStyling(ContentCssSchema.INSTANCE)
        .toFactory()
}
