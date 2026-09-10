package com.wafflestudio.csereal.common.sanitize

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import org.owasp.html.CssSchema

/**
 * 본문 style 속성에서 살릴 CSS 속성.
 *
 * 기준은 "코퍼스에 있으니 넣는다"가 아니라 **"빼봤더니 화면이 바뀌니 넣는다"** 다.
 * 정책 밖 50종을 그 속성을 쓰는 **모든** 운영 문서에서 재니 43종이 한 픽셀도 안 바뀌었다 —
 * `cursor`(173문서)·`border-image`(130)·`font-variant-caps`(106)처럼 많이 쓰여도 그렇다.
 *
 * 나머지 일곱은 화면이 바뀌는데도 일부러 뺀 것들이다: [DROPPED] 의 폰트 둘,
 * 상자를 벗어나는 `position`, 남의 페이지에서 딸려온 Tailwind 리셋(`flex-direction`·`gap`),
 * 그리고 브라우저 기본값과 다를 게 없는 `font-kerning`·`opacity`.
 *
 * 측정 절차·근거·스크린샷은 `docs/css-allowlist/README.md`. 새 속성도 같은 근거를 요구한다.
 */
object ContentCssSchema {

    /** OWASP 가 정의는 했지만 DEFAULT 에서 뺀 것. 상자를 벗어나는 축이 아니라 다시 켠다. */
    private val REENABLED = setOf("display", "float")

    /** OWASP 에 정의가 없어 허용 값을 직접 적는 것. bits=0 은 리터럴만 받는다는 뜻. */
    private val CUSTOM: Map<String, CssSchema.Property> = mapOf(
        // 247문서. keep-all 이 빠지면 한글 줄바꿈이 어긋난다.
        "word-break" to CssSchema.Property(
            0,
            ImmutableSet.of("keep-all", "break-all", "break-word", "normal", "inherit", "initial"),
            ImmutableMap.of()
        ),
        // 36문서. shorthand `text-decoration` 은 DEFAULT 에 있는데 longhand 만 빠져 있다.
        "text-decoration-line" to CssSchema.Property(
            0,
            ImmutableSet.of("underline", "overline", "line-through", "none", "inherit", "initial"),
            ImmutableMap.of()
        )
    )

    /**
     * 본문 폰트를 뷰어 기본값으로 통일한다.
     *
     * ⚠️ 되돌리면 세탁이 멱등하지 않게 된다 — OWASP 가 따옴표 없는 한글 폰트명을 통과시키면서
     * 출력엔 따옴표를 붙이는데, 따옴표 안 비ASCII 는 다음 세탁에서 버려진다.
     * 저장할 때마다 글이 조금씩 깎인다(코퍼스 400건 위반 42건 → 0건).
     */
    private val DROPPED = setOf("font-family", "font")

    val INSTANCE: CssSchema = CssSchema.union(
        CssSchema.withProperties(CssSchema.DEFAULT.allowedProperties() - DROPPED + REENABLED),
        CssSchema.withProperties(CUSTOM)
    )
}
