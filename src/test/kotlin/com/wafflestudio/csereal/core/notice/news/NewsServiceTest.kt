package com.wafflestudio.csereal.core.notice.news

import com.wafflestudio.csereal.core.news.database.NewsEntity
import com.wafflestudio.csereal.core.news.database.NewsRepository
import com.wafflestudio.csereal.core.news.dto.NewsDto
import com.wafflestudio.csereal.core.news.service.NewsService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest
class NewsServiceTest(
    private val newsService: NewsService,
    private val newsRepository: NewsRepository,
) : BehaviorSpec() {
    init {

        afterSpec {
            newsRepository.deleteAll()
        }

        Given("뉴스를 생성하려고 할 때 간단한 뉴스가 주어지면") {
            val newsDTO = NewsDto(
                id = -1,
                title = "title",
                description = """
                        <h1>Hello, World!</h1>
                        <p>This is news description.</p>
                        <h3>Goodbye, World!</h3>
                    """.trimIndent(),
                tags = emptyList(),
                createdAt = null,
                modifiedAt = null,
                isPrivate = false,
                isSlide = false,
                isImportant = false,
                prevId = null,
                prevTitle = null,
                nextId = null,
                nextTitle = null,
                imageURL = null,
                attachments = null,
            )

            When("DTO를 이용하여 뉴스를 생성하면") {
                val createdNewsDTO = newsService.createNews(newsDTO, null, null)

                Then("뉴스가 생성되어야 한다.") {
                    newsRepository.count() shouldBe 1
                    newsRepository.findByIdOrNull(createdNewsDTO.id) shouldNotBe null
                }

                Then("plainTextDescription이 생성되었어야 한다.") {
                    val createdNewsEntity = newsRepository.findByIdOrNull(createdNewsDTO.id)!!
                    createdNewsEntity.plainTextDescription shouldBe "Hello, World! This is news description. Goodbye, World!"
                }
            }
        }

        Given("간단한 뉴스가 저장되어 있을 때") {
            val newsEntity = newsRepository.save(
                NewsEntity(
                    title = "title",
                    description = """
                            <h1>Hello, World!</h1>
                            <p>This is news description.</p>
                            <h3>Goodbye, World!</h3>
                            """.trimIndent(),
                    plainTextDescription = "Hello, World! This is news description. Goodbye, World!",
                    isPrivate = false,
                    isSlide = false,
                    isImportant = false,
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
                    null,
                    emptyList()
                )

                Then("description, plainTextDescription이 수정되어야 한다.") {
                    val updatedNewsEntity = newsRepository.findByIdOrNull(newsEntity.id)!!
                    updatedNewsEntity.description shouldBe """
                            <h1>Hello, World!</h1>
                            <p>This is modified news description.</p>
                            <h3>Goodbye, World!</h3>
                            <p>This is additional description.</p>
                            """.trimIndent()
                    updatedNewsEntity.plainTextDescription shouldBe "Hello, World! This is modified news description. Goodbye, World! This is additional description."
                }
            }
        }
    }
}
