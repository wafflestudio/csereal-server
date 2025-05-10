package com.wafflestudio.csereal.core.notice.news

import com.wafflestudio.csereal.core.news.database.NewsEntity
import com.wafflestudio.csereal.core.news.database.NewsRepository
import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.news.service.NewsService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime

@SpringBootTest
class NewsServiceTest(
    private val newsService: NewsService,
    private val newsRepository: NewsRepository
) : BehaviorSpec() {
    init {
        extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

        afterSpec {
            newsRepository.deleteAll()
        }

        Given("뉴스를 생성하려고 할 때 간단한 뉴스가 주어지면") {
            val newsDTO = NewsDto(
                id = -1,
                title = "title",
                titleForMain = null,
                description = """
                        <h1>Hello, World!</h1>
                        <p>This is news description.</p>
                        <h3>Goodbye, World!</h3>
                """.trimIndent(),
                tags = emptyList(),
                createdAt = null,
                modifiedAt = null,
                date = LocalDateTime.now(),
                isPrivate = false,
                isSlide = false,
                isImportant = false,
                importantUntil = null,
                prevId = null,
                prevTitle = null,
                nextId = null,
                nextTitle = null,
                imageURL = null,
                attachments = null
            )

            When("DTO를 이용하여 뉴스를 생성하면") {
                val createdNewsDTO = newsService.createNews(newsDTO, null, null)

                Then("뉴스가 생성되어야 한다.") {
                    newsRepository.count() shouldBe 1
                    newsRepository.findByIdOrNull(createdNewsDTO.id) shouldNotBe null
                }

                Then("plainTextDescription이 생성되었어야 한다.") {
                    val createdNewsEntity = newsRepository.findByIdOrNull(createdNewsDTO.id)!!
                    createdNewsEntity.plainTextDescription shouldBe (
                        "Hello, World! This is news description. Goodbye, World!"
                        )
                }
            }
        }

        Given("간단한 뉴스가 저장되어 있을 때") {
            val newsEntity = newsRepository.save(
                NewsEntity(
                    title = "title",
                    titleForMain = null,
                    description = """
                            <h1>Hello, World!</h1>
                            <p>This is news description.</p>
                            <h3>Goodbye, World!</h3>
                    """.trimIndent(),
                    plainTextDescription = "Hello, World! This is news description. Goodbye, World!",
                    date = LocalDateTime.now(),
                    isPrivate = false,
                    isSlide = false,
                    isImportant = false
                )
            )

            When("저장된 뉴스의 Description을 수정하면") {
                newsService.updateNews(
                    newsEntity.id,
                    NewsDto.of(newsEntity, null, emptyList(), null)
                        .copy(
                            description = """
                            <h1>Hello, World!</h1>
                            <p>This is modified news description.</p>
                            <h3>Goodbye, World!</h3>
                            <p>This is additional description.</p>
                            """.trimIndent()
                        ),
                    null,
                    null
                )

                Then("description, plainTextDescription이 수정되어야 한다.") {
                    val updatedNewsEntity = newsRepository.findByIdOrNull(newsEntity.id)!!
                    updatedNewsEntity.description shouldBe """
                            <h1>Hello, World!</h1>
                            <p>This is modified news description.</p>
                            <h3>Goodbye, World!</h3>
                            <p>This is additional description.</p>
                    """.trimIndent()
                    updatedNewsEntity.plainTextDescription shouldBe (
                        "Hello, World! This is modified news description." +
                            " Goodbye, World! This is additional description."
                        )
                }
            }
        }

        Given("Slide된 뉴스 2개, Slide되지 않은 뉴스가 2개 있을 때") {
            val newsEntitySlide1 = newsRepository.save(
                NewsEntity(
                    title = "title",
                    titleForMain = null,
                    description = """
                            <h1>Hello, World!</h1>
                            <p>This is news description.</p>
                            <h3>Goodbye, World!</h3>
                    """.trimIndent(),
                    plainTextDescription = "Hello, World! This is news description. Goodbye, World!",
                    date = LocalDateTime.now(),
                    isPrivate = false,
                    isSlide = true,
                    isImportant = false
                )
            )

            val newsEntitySlide2 = newsRepository.save(
                NewsEntity(
                    title = "title",
                    titleForMain = null,
                    description = """
                            <h1>Hello, World!</h1>
                            <p>This is news description.</p>
                            <h3>Goodbye, World!</h3>
                    """.trimIndent(),
                    plainTextDescription = "Hello, World! This is news description. Goodbye, World!",
                    date = LocalDateTime.now(),
                    isPrivate = false,
                    isSlide = true,
                    isImportant = false
                )
            )

            val newsEntityNoSlide1 = newsRepository.save(
                NewsEntity(
                    title = "title",
                    titleForMain = null,
                    description = """
                            <h1>Hello, World!</h1>
                            <p>This is news description.</p>
                            <h3>Goodbye, World!</h3>
                    """.trimIndent(),
                    plainTextDescription = "Hello, World! This is news description. Goodbye, World!",
                    date = LocalDateTime.now(),
                    isPrivate = false,
                    isSlide = false,
                    isImportant = false
                )
            )

            val newsEntityNoSlide2 = newsRepository.save(
                NewsEntity(
                    title = "title",
                    titleForMain = null,
                    description = """
                            <h1>Hello, World!</h1>
                            <p>This is news description.</p>
                            <h3>Goodbye, World!</h3>
                    """.trimIndent(),
                    plainTextDescription = "Hello, World! This is news description. Goodbye, World!",
                    date = LocalDateTime.now(),
                    isPrivate = false,
                    isSlide = false,
                    isImportant = false
                )
            )

            When("Slide한 뉴스 2페이지를 가져오면") {
                val response = newsService.readAllSlides(1, 1)

                Then("Slide된 뉴스 2개 중 먼저 생성된 1개가 나와야 한다.") {
                    response.slides.size shouldBe 1
                    response.slides.first().id shouldBe newsEntitySlide1.id
                }

                Then("총 개수가 2개가 나와야 한다.") {
                    response.total shouldBe 2
                }
            }
        }
    }
}
