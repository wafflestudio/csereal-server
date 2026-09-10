package com.wafflestudio.csereal.common.sanitize

import com.wafflestudio.csereal.core.news.api.req.CreateNewsReq
import com.wafflestudio.csereal.core.news.api.req.UpdateNewsReq
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
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

/**
 * 리스너가 **저장 경로에서 실제로** 도는지 본다.
 *
 * 순수 단위 테스트로는 확인할 수 없는 것이 둘이다 —
 * `@PreUpdate` 안에서 필드를 바꿔도 Hibernate 가 만드는 UPDATE 문에 반영되는가,
 * 그리고 본문에서 파생된 평문이 세탁된 값에서 나오는가.
 */
@ActiveProfiles("test")
@SpringBootTest
@Import(MySQLTestContainerConfig::class)
class SanitizeEntityListenerTest(
    private val newsService: NewsService,
    private val newsRepository: NewsRepository
) : BehaviorSpec() {
    init {
        extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

        afterSpec { newsRepository.deleteAll() }

        val dirty = """<p>본문</p><script>alert(1)</script><div style="display:none">숨은 글</div>"""

        fun create(description: String) = CreateNewsReq(
            title = "제목",
            titleForMain = null,
            description = description,
            date = LocalDateTime.now(),
            isPrivate = false,
            isSlide = false,
            isImportant = false,
            importantUntil = null,
            tags = emptyList()
        )

        Given("새 글을 저장하면") {
            val created = newsService.createNews(create(dirty), null, null)
            val saved = newsRepository.findByIdOrNull(created.id)!!

            Then("@PrePersist 가 돌아 세탁된 본문이 저장된다") {
                saved.description shouldContain "본문"
                saved.description shouldNotContain "script"
                saved.description shouldNotContain "숨은 글"
            }

            Then("파생 평문도 세탁된 본문에서 나온다") {
                saved.plainTextDescription shouldContain "본문"
                saved.plainTextDescription shouldNotContain "숨은 글"
            }
        }

        Given("기존 글의 본문을 고치면") {
            val created = newsService.createNews(create("<p>처음</p>"), null, null)
            newsService.updateNews(
                created.id,
                UpdateNewsReq(
                    title = "제목",
                    titleForMain = null,
                    description = dirty,
                    date = LocalDateTime.now(),
                    isPrivate = false,
                    isSlide = false,
                    isImportant = false,
                    importantUntil = null,
                    tags = emptyList(),
                    attachmentIds = emptyList(),
                    removeImage = false
                ),
                null,
                null
            )
            val updated = newsRepository.findByIdOrNull(created.id)!!

            Then("@PreUpdate 가 돌아 세탁된 본문이 저장된다") {
                updated.description shouldContain "본문"
                updated.description shouldNotContain "script"
                updated.description shouldNotContain "숨은 글"
            }

            Then("파생 평문이 함께 갱신된다") {
                updated.plainTextDescription shouldNotContain "숨은 글"
                updated.plainTextDescription shouldNotContain "처음"
            }
        }

        Given("이미 세탁된 글을 다시 저장하면") {
            val created = newsService.createNews(create(dirty), null, null)
            val once = newsRepository.findByIdOrNull(created.id)!!.description

            newsService.updateNews(
                created.id,
                UpdateNewsReq(
                    title = "제목 수정",
                    titleForMain = null,
                    description = once,
                    date = LocalDateTime.now(),
                    isPrivate = false,
                    isSlide = false,
                    isImportant = false,
                    importantUntil = null,
                    tags = emptyList(),
                    attachmentIds = emptyList(),
                    removeImage = false
                ),
                null,
                null
            )

            // 이게 깨지면 글이 편집될 때마다 조금씩 깎이고 backfill 을 다시 돌릴 수 없다.
            Then("본문이 더 깎이지 않는다") {
                newsRepository.findByIdOrNull(created.id)!!.description shouldBe once
            }
        }
    }
}
