package com.wafflestudio.csereal.core.news.scheduler

import com.wafflestudio.csereal.core.news.database.NewsEntity
import com.wafflestudio.csereal.core.news.database.NewsRepository
import com.wafflestudio.csereal.global.config.MySQLTestContainerConfig
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@SpringBootTest
@Transactional
@Import(MySQLTestContainerConfig::class)
class NewsSchedulerTest(
    private val newsScheduler: NewsScheduler,
    private val newsRepository: NewsRepository
) : BehaviorSpec() {

    private val KST: ZoneId = ZoneId.of("Asia/Seoul")
    private val today: LocalDate = LocalDate.now(KST)
    private val yesterday: LocalDate = today.minusDays(1)
    private val tomorrow: LocalDate = today.plusDays(1)

    init {
        extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

        afterSpec {
            newsRepository.deleteAll()
        }

        Given(
            "Various news items with different importance " +
                "expiration dates are present"
        ) {
            val newsExpiredImportant = createTestNews(
                title = "Expired Important News",
                isImportant = true,
                importantUntil = yesterday
            )

            val newsTodayImportant = createTestNews(
                title = "Today Important News",
                isImportant = true,
                importantUntil = today
            )

            val newsFutureImportant = createTestNews(
                title = "Future Important News",
                isImportant = true,
                importantUntil = tomorrow
            )

            val newsPermanentImportant = createTestNews(
                title = "Permanent Important News",
                isImportant = true,
                importantUntil = null
            )

            val newsNotImportant = createTestNews(
                title = "Not Important News",
                isImportant = false
            )

            When("The updateNewsExpirationStatus scheduler task runs") {
                newsScheduler.updateNewsExpirationStatus()

                Then(
                    "News items with importance expiration date before today " +
                        "should be updated"
                ) {
                    val updatedExpiredImportant = newsRepository.findByIdOrNull(newsExpiredImportant.id)!!
                    updatedExpiredImportant.isImportant shouldBe false
                    updatedExpiredImportant.importantUntil shouldBe null
                }

                Then(
                    "News items with importance expiration date today or later, " +
                        "or no expiration date, should remain unchanged"
                ) {
                    val updatedTodayImportant = newsRepository.findByIdOrNull(newsTodayImportant.id)!!
                    updatedTodayImportant.isImportant shouldBe true
                    updatedTodayImportant.importantUntil shouldBe today

                    val updatedFutureImportant = newsRepository.findByIdOrNull(newsFutureImportant.id)!!
                    updatedFutureImportant.isImportant shouldBe true
                    updatedFutureImportant.importantUntil shouldBe tomorrow

                    val updatedPermanentImportant = newsRepository.findByIdOrNull(newsPermanentImportant.id)!!
                    updatedPermanentImportant.isImportant shouldBe true
                    updatedPermanentImportant.importantUntil shouldBe null

                    val updatedNotImportant = newsRepository.findByIdOrNull(newsNotImportant.id)!!
                    updatedNotImportant.isImportant shouldBe false
                    updatedNotImportant.importantUntil shouldBe null
                }
            }
        }
    }

    private fun createTestNews(
        title: String,
        isImportant: Boolean = false,
        importantUntil: LocalDate? = null
    ): NewsEntity {
        return newsRepository.save(
            NewsEntity(
                title = title,
                titleForMain = null,
                description = "Test description for $title",
                plainTextDescription = "Test plain text description for $title",
                date = today.atStartOfDay(),
                isPrivate = false,
                isSlide = false,
                isImportant = isImportant,
                importantUntil = importantUntil,
                mainImage = null,
                attachments = mutableListOf(),
                newsTags = mutableSetOf()
            )
        )
    }
}
