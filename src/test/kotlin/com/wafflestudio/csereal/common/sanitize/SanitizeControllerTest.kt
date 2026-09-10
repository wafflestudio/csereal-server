package com.wafflestudio.csereal.common.sanitize

import com.wafflestudio.csereal.common.properties.EndpointProperties
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.nio.file.Files
import kotlin.io.path.absolutePathString

/**
 * HTTP 경계만 본다 — 세탁 규칙 자체는 [ContentSanitizerTest] 가 지킨다.
 *
 * 여기서 확인하는 것은 `text/html` 본문이 `@RequestBody String` 으로 들어오고
 * 세탁된 HTML 이 그대로 나가는가다. 컨버터가 안 붙으면 415 가 난다.
 */
class SanitizeControllerTest : StringSpec({
    val uploadDir = Files.createTempDirectory("sanitize-controller-test")
    val sanitizer = ContentSanitizer(
        InlineImageExtractor(
            uploadPath = uploadDir.absolutePathString() + "/",
            endpointProperties = EndpointProperties(
                frontend = "https://cse.snu.ac.kr",
                backend = "https://cse.snu.ac.kr/api"
            )
        )
    )
    val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(SanitizeController(sanitizer)).build()

    // charset 을 일부러 안 붙인다 — 붙여넣기 클라이언트가 빠뜨려도 한글이 살아야 한다.
    fun sanitize(html: String, contentType: MediaType = MediaType.TEXT_HTML) =
        mockMvc.perform(
            post("/api/v2/content/sanitize")
                .contentType(contentType)
                .content(html.toByteArray(Charsets.UTF_8))
        )

    fun bodyOf(result: org.springframework.test.web.servlet.ResultActions) =
        result.andReturn().response.contentAsByteArray.toString(Charsets.UTF_8)

    "text/html 본문을 받아 세탁된 HTML 을 돌려준다" {
        val body = bodyOf(sanitize("""<p>본문</p><script>alert(1)</script>""").andExpect(status().isOk))

        body shouldContain "본문"
        body shouldNotContain "script"
    }

    "Content-Type 에 charset 이 없어도 한글이 안 깨진다" {
        // String 으로 받으면 Spring 이 ISO-8859-1 로 디코드해 "ë³¸ë¬¸" 이 된다.
        bodyOf(sanitize("<p>공지사항 안내드립니다</p>")) shouldContain "공지사항 안내드립니다"
    }

    "text/plain 으로 보내도 받는다 — 에디터가 무엇으로 보낼지 강제하지 않는다" {
        bodyOf(
            sanitize("""<p onmouseover=alert(1)>x</p>""", MediaType.TEXT_PLAIN).andExpect(status().isOk)
        ) shouldNotContain "onmouseover"
    }

    "빈 본문도 200 이다 — 붙여넣기가 비어 있을 수 있다" {
        sanitize("").andExpect(status().isOk)
    }
})
