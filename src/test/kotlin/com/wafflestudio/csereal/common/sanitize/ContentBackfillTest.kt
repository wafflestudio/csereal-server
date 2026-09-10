package com.wafflestudio.csereal.common.sanitize

import com.wafflestudio.csereal.core.news.api.req.CreateNewsReq
import com.wafflestudio.csereal.core.news.database.NewsRepository
import com.wafflestudio.csereal.core.news.service.NewsService
import com.wafflestudio.csereal.global.config.MySQLTestContainerConfig
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

/**
 * 리스너를 거치지 않고 들어간 더러운 본문(= 세탁 도입 전 데이터)을 SQL 로 심어 두고 backfill 이 고치는지 본다.
 */
@ActiveProfiles("test")
@SpringBootTest
@Import(MySQLTestContainerConfig::class)
class ContentBackfillTest(
    private val backfill: ContentBackfill,
    private val newsService: NewsService,
    private val newsRepository: NewsRepository,
    private val jdbc: JdbcTemplate
) : BehaviorSpec() {
    init {
        extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

        afterSpec { newsRepository.deleteAll() }

        val dirty = """<p style="mso-bidi-font-size:12pt;color:red">본문</p>""" +
            """<script>alert(1)</script><p style="display:none">숨은 글</p>"""

        Given("세탁 전에 저장된 더러운 본문이 있으면") {
            val created = newsService.createNews(
                CreateNewsReq(
                    title = "제목",
                    titleForMain = null,
                    description = "<p>깨끗</p>",
                    date = LocalDateTime.now(),
                    isPrivate = false,
                    isSlide = false,
                    isImportant = false,
                    importantUntil = null,
                    tags = emptyList()
                ),
                null,
                null
            )
            // 리스너를 우회해 옛 데이터 모양을 만든다.
            jdbc.update(
                "UPDATE news SET description = ?, plain_text_description = ? WHERE id = ?",
                dirty,
                "본문 숨은 글",
                created.id
            )

            val first = backfill.run()
            val saved = newsRepository.findByIdOrNull(created.id)!!

            Then("세탁된 본문으로 바뀐다") {
                first["news"] shouldBe 1
                saved.description shouldContain """<p style="color:red">본문</p>"""
                saved.description shouldNotContain "mso-bidi"
                saved.description shouldNotContain "script"
                saved.description shouldNotContain "숨은 글"
            }

            Then("파생 평문도 다시 계산된다") {
                saved.plainTextDescription shouldBe "본문"
            }

            Then("다시 돌리면 바뀌는 행이 없다") {
                backfill.run()["news"] shouldBe 0
            }
        }
    }
}
