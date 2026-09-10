package com.wafflestudio.csereal.common.sanitize

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor
import org.springframework.stereotype.Component

/**
 * 본문 HTML 을 저장 가능한 형태로 만든다.
 *
 * ```
 * ① 전처리   숨김 요소·빈 이미지 제거, data:image → 파일 추출
 * ② 세탁     태그·속성·CSS 허용 목록 (OWASP)
 * ③ 후처리   링크 rel 부착, 평문 URL 자동 링크
 * ```
 *
 * ①이 ②보다 먼저인 건 강제다 — 정책이 `data:` 를 안 받아 세탁이 `src` 를 지운다.
 *
 * **멱등이다.** 저장할 때마다 도는 함수라 깨지면 글이 편집할 때마다 조금씩 깎이고
 * backfill 을 다시 돌릴 수 없다. 테스트가 지킨다.
 */
@Component
class ContentSanitizer(
    private val inlineImageExtractor: InlineImageExtractor
) {
    fun sanitize(html: String): String {
        if (html.isBlank()) return html

        val doc = parse(html)
        prepare(doc)

        val cleaned = parse(ContentPolicy.INSTANCE.sanitize(doc.body().html()))
        finish(cleaned)

        return cleaned.body().html()
    }

    // jsoup 기본값은 pretty-print 다. 켜 두면 태그 사이에 들여쓰기 공백이 끼어 표가 많은
    // 본문은 오히려 커지고, 인라인 요소 사이 공백은 렌더에도 나타난다.
    private fun parse(html: String): Document =
        Jsoup.parseBodyFragment(html).also { it.outputSettings().prettyPrint(false) }

    /** ① 세탁이 판단할 수 없는 것들을 먼저 걷어낸다. */
    private fun prepare(doc: Document) {
        // 남의 페이지를 복사하며 딸려온 UI 뼈대(#AutoComplete, .sr-filters)와 문법검사 확장의 iframe.
        // 서식이 아니라 잔재라 요소째 지운다 — 그래야 안의 텍스트가 검색 색인에도 안 남는다.
        doc.select("[style]").forEach {
            if (HIDDEN.containsMatchIn(it.attr("style"))) it.remove()
        }

        // src 없는 img(코퍼스에 8개, 전부 무의미). 두면 세탁이 position 을 뗀 뒤 빈 상자가 드러난다.
        doc.select("img:not([src])").forEach { it.remove() }

        inlineImageExtractor.extract(doc)
    }

    /** ③ 세탁을 통과한 것에만 적용한다. */
    private fun finish(doc: Document) {
        // target 은 건드리지 않는다 — 원문이 정한 것을 존중한다.
        doc.select("a[href]").forEach { it.attr("rel", "noopener noreferrer") }
        autolink(doc)
    }

    /**
     * 평문 URL·메일 주소를 링크로 만든다.
     *
     * **텍스트 노드만** 훑는다 — 원시 문자열을 정규식으로 훑으면 `<pre>` 안의 명령줄이나
     * 속성값 속 주소까지 링크가 된다(프론트 Autolinker 에서 겪은 문제다).
     */
    private fun autolink(doc: Document) {
        val targets = mutableListOf<TextNode>()
        doc.body().traverse(object : NodeVisitor {
            override fun head(node: Node, depth: Int) {
                if (node !is TextNode) return
                if (isInside(node, AUTOLINK_EXCLUDED)) return
                if (LINKABLE.containsMatchIn(node.text())) targets += node
            }
        })

        for (node in targets) {
            node.before(linkify(node.text()))
            node.remove()
        }
    }

    /** 조상 중에 [tags] 에 든 요소가 있나. */
    private fun isInside(node: Node, tags: Set<String>): Boolean {
        var parent = node.parent()
        while (parent != null) {
            if (parent is Element && parent.tagName() in tags) return true
            parent = parent.parent()
        }
        return false
    }

    /** 매치 경계로 잘라 링크와 평문을 번갈아 조립한다. 링크 밖은 반드시 이스케이프해야 원본의 `<` 가 안전하다. */
    private fun linkify(source: String): String {
        val out = StringBuilder()
        var last = 0
        for (m in LINKABLE.findAll(source)) {
            out.append(escape(source.substring(last, m.range.first)))
            val text = m.value
            val href = if (text.contains('@')) "mailto:$text" else text
            out.append("""<a href="${escape(href)}" target="_blank" rel="noopener noreferrer">${escape(text)}</a>""")
            last = m.range.last + 1
        }
        out.append(escape(source.substring(last)))
        return out.toString()
    }

    private fun escape(s: String) =
        s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    companion object {
        private val HIDDEN = Regex("""display\s*:\s*none""", RegexOption.IGNORE_CASE)

        /** 이미 링크이거나, 주소가 그대로 보여야 하는 곳. */
        private val AUTOLINK_EXCLUDED = setOf("a", "pre", "code")

        /**
         * ⚠️ 스킴 없는 도메인은 일부러 뺐다 — 받으면 본문의 `모집요강.pdf`·`서식.hwp` 가
         * TLD 로 보여 링크가 된다(프론트도 같은 이유로 `tldMatches: false` 였다).
         * 끝의 문장부호는 URL 에서 뺀다.
         */
        private val LINKABLE = Regex(
            """(?:https?://[^\s<>"']+|[\w.+-]+@[\w-]+(?:\.[\w-]+)+)(?<![.,;:!?)\]])"""
        )
    }
}
