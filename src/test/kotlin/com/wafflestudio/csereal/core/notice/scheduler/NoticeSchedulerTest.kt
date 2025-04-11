package com.wafflestudio.csereal.core.notice.scheduler

import com.wafflestudio.csereal.core.notice.database.NoticeEntity
import com.wafflestudio.csereal.core.notice.database.NoticeRepository
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@SpringBootTest
@Transactional
class NoticeSchedulerTest(
    private val noticeScheduler: NoticeScheduler,
    private val noticeRepository: NoticeRepository,
    private val userRepository: UserRepository
) : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    private lateinit var user: UserEntity
    private val KST: ZoneId = ZoneId.of("Asia/Seoul")
    private val today: LocalDate = LocalDate.now(KST)
    private val yesterday: LocalDate = today.minusDays(1)
    private val tomorrow: LocalDate = today.plusDays(1)

    init {
        // Setup: Create a user first
        beforeSpec {
            user = userRepository.save(UserEntity("username", "name", "email", "studentId"))
        }

        // Cleanup
        afterSpec {
            noticeRepository.deleteAll()
            userRepository.deleteAll()
        }

        Given("Various notices with different expiration dates are present") {
            // Create test notices
            val noticeExpiredPinned = createTestNotice("Expired Pinned", isPinned = true, pinnedUntil = yesterday)
            val noticeExpiredImportant = createTestNotice("Expired Important", isImportant = true, importantUntil = yesterday)
            val noticeTodayPinned = createTestNotice("Today Pinned", isPinned = true, pinnedUntil = today)
            val noticeTodayImportant = createTestNotice("Today Important", isImportant = true, importantUntil = today)
            val noticeFuturePinned = createTestNotice("Future Pinned", isPinned = true, pinnedUntil = tomorrow)
            val noticeFutureImportant = createTestNotice("Future Important", isImportant = true, importantUntil = tomorrow)
            val noticePermanentPinned = createTestNotice("Permanent Pinned", isPinned = true, pinnedUntil = null)
            val noticePermanentImportant = createTestNotice("Permanent Important", isImportant = true, importantUntil = null)
            val noticeNotPinnedOrImportant = createTestNotice("Not Pinned or Important", isPinned = false, isImportant = false)

            When("The updateNoticeExpirationStatus scheduler task runs") {
                noticeScheduler.updateNoticeExpirationStatus()

                Then("Notices with expiration date before today should be updated") {
                    val updatedExpiredPinned = noticeRepository.findByIdOrNull(noticeExpiredPinned.id)!!
                    updatedExpiredPinned.isPinned shouldBe false
                    updatedExpiredPinned.pinnedUntil shouldBe null

                    val updatedExpiredImportant = noticeRepository.findByIdOrNull(noticeExpiredImportant.id)!!
                    updatedExpiredImportant.isImportant shouldBe false
                    updatedExpiredImportant.importantUntil shouldBe null
                }

                Then("Notices with expiration date today or later, or no expiration date, should remain unchanged") {
                    // Today
                    val updatedTodayPinned = noticeRepository.findByIdOrNull(noticeTodayPinned.id)!!
                    updatedTodayPinned.isPinned shouldBe true
                    updatedTodayPinned.pinnedUntil shouldBe today

                    val updatedTodayImportant = noticeRepository.findByIdOrNull(noticeTodayImportant.id)!!
                    updatedTodayImportant.isImportant shouldBe true
                    updatedTodayImportant.importantUntil shouldBe today

                    // Future
                    val updatedFuturePinned = noticeRepository.findByIdOrNull(noticeFuturePinned.id)!!
                    updatedFuturePinned.isPinned shouldBe true
                    updatedFuturePinned.pinnedUntil shouldBe tomorrow

                    val updatedFutureImportant = noticeRepository.findByIdOrNull(noticeFutureImportant.id)!!
                    updatedFutureImportant.isImportant shouldBe true
                    updatedFutureImportant.importantUntil shouldBe tomorrow

                    // Permanent
                    val updatedPermanentPinned = noticeRepository.findByIdOrNull(noticePermanentPinned.id)!!
                    updatedPermanentPinned.isPinned shouldBe true
                    updatedPermanentPinned.pinnedUntil shouldBe null

                    val updatedPermanentImportant = noticeRepository.findByIdOrNull(noticePermanentImportant.id)!!
                    updatedPermanentImportant.isImportant shouldBe true
                    updatedPermanentImportant.importantUntil shouldBe null

                    // Neither
                    val updatedNotPinnedOrImportant = noticeRepository.findByIdOrNull(noticeNotPinnedOrImportant.id)!!
                    updatedNotPinnedOrImportant.isPinned shouldBe false
                    updatedNotPinnedOrImportant.pinnedUntil shouldBe null
                    updatedNotPinnedOrImportant.isImportant shouldBe false
                    updatedNotPinnedOrImportant.importantUntil shouldBe null
                }
            }
        }
    }

    private fun createTestNotice(
        title: String,
        isPinned: Boolean = false,
        pinnedUntil: LocalDate? = null,
        isImportant: Boolean = false,
        importantUntil: LocalDate? = null
    ): NoticeEntity {
        return noticeRepository.save(
            NoticeEntity(
                title = title,
                titleForMain = null,
                description = "Test description for $title",
                plainTextDescription = "Test description for $title",
                isPrivate = false,
                isPinned = isPinned,
                pinnedUntil = pinnedUntil,
                isImportant = isImportant,
                importantUntil = importantUntil,
                author = user
            )
        )
    }
} 
