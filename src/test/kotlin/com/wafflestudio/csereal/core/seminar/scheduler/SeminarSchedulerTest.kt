package com.wafflestudio.csereal.core.seminar.scheduler

import com.wafflestudio.csereal.core.seminar.database.SeminarEntity
import com.wafflestudio.csereal.core.seminar.database.SeminarRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@SpringBootTest
@Transactional
class SeminarSchedulerTest(
    private val seminarScheduler: SeminarScheduler,
    private val seminarRepository: SeminarRepository
) : BehaviorSpec() {

    private val KST: ZoneId = ZoneId.of("Asia/Seoul")
    private val today: LocalDate = LocalDate.now(KST)
    private val yesterday: LocalDate = today.minusDays(1)
    private val tomorrow: LocalDate = today.plusDays(1)

    init {
        extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

        beforeSpec {
        }

        afterSpec {
            seminarRepository.deleteAll()
        }

        Given(
            "Various seminars with different importance " +
                "expiration dates are present"
        ) {
            val seminarExpiredImportant = createTestSeminar(
                title = "Expired Important Seminar",
                isImportant = true,
                importantUntil = yesterday
            )

            val seminarTodayImportant = createTestSeminar(
                title = "Today Important Seminar",
                isImportant = true,
                importantUntil = today
            )

            val seminarFutureImportant = createTestSeminar(
                title = "Future Important Seminar",
                isImportant = true,
                importantUntil = tomorrow
            )

            val seminarPermanentImportant = createTestSeminar(
                title = "Permanent Important Seminar",
                isImportant = true,
                importantUntil = null
            )

            val seminarNotImportant = createTestSeminar(
                title = "Not Important Seminar",
                isImportant = false
            )

            When("The updateSeminarExpirationStatus scheduler task runs") {
                seminarScheduler.updateSeminarExpirationStatus()

                Then(
                    "Seminars with importance expiration date before today " +
                        "should be updated"
                ) {
                    val updatedExpiredImportant = seminarRepository.findByIdOrNull(seminarExpiredImportant.id)!!
                    updatedExpiredImportant.isImportant shouldBe false
                    updatedExpiredImportant.importantUntil shouldBe null
                }

                Then(
                    "Seminars with importance expiration date today or later, " +
                        "or no expiration date, should remain unchanged"
                ) {
                    val updatedTodayImportant = seminarRepository.findByIdOrNull(seminarTodayImportant.id)!!
                    updatedTodayImportant.isImportant shouldBe true
                    updatedTodayImportant.importantUntil shouldBe today

                    val updatedFutureImportant = seminarRepository.findByIdOrNull(seminarFutureImportant.id)!!
                    updatedFutureImportant.isImportant shouldBe true
                    updatedFutureImportant.importantUntil shouldBe tomorrow

                    val updatedPermanentImportant = seminarRepository.findByIdOrNull(seminarPermanentImportant.id)!!
                    updatedPermanentImportant.isImportant shouldBe true
                    updatedPermanentImportant.importantUntil shouldBe null

                    val updatedNotImportant = seminarRepository.findByIdOrNull(seminarNotImportant.id)!!
                    updatedNotImportant.isImportant shouldBe false
                    updatedNotImportant.importantUntil shouldBe null
                }
            }
        }
    }

    private fun createTestSeminar(
        title: String,
        isImportant: Boolean = false,
        importantUntil: LocalDate? = null
    ): SeminarEntity {
        return seminarRepository.save(
            SeminarEntity(
                title = title,
                titleForMain = "Title for Main: $title",
                description = "Test description for $title",
                plainTextDescription = "Test plain text description for $title",
                introduction = "Test introduction for $title",
                plainTextIntroduction = "Test plain text introduction for $title",
                name = "Seminar Speaker Name",
                speakerURL = null,
                speakerTitle = "Speaker Title",
                affiliation = "Speaker Affiliation",
                affiliationURL = null,
                startDate = LocalDateTime.now(),
                endDate = LocalDateTime.now().plusHours(1),
                location = "Room 401",
                host = "CSE department",
                isPrivate = false,
                isImportant = isImportant,
                importantUntil = importantUntil,
                additionalNote = "Additional note for $title",
                plainTextAdditionalNote = "Additional note for $title",
                mainImage = null,
                attachments = mutableListOf()
            )
        )
    }
}
