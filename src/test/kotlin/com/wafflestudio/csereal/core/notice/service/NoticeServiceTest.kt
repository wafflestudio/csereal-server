package com.wafflestudio.csereal.core.notice.service

import com.wafflestudio.csereal.core.notice.database.NoticeEntity
import com.wafflestudio.csereal.core.notice.database.NoticeRepository
import com.wafflestudio.csereal.core.notice.api.req.CreateNoticeReq
import com.wafflestudio.csereal.core.notice.api.req.UpdateNoticeReq
import com.wafflestudio.csereal.core.user.database.UserRepository
import com.wafflestudio.csereal.core.user.service.UserService
import com.wafflestudio.csereal.global.authenticateAs
import com.wafflestudio.csereal.global.config.MySQLTestContainerConfig
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.test.context.ActiveProfiles
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@Import(MySQLTestContainerConfig::class)
class NoticeServiceTest(
    private val noticeService: NoticeService,
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val noticeRepository: NoticeRepository
) : BehaviorSpec() {
    init {
        beforeSpec {
            authenticateAs(userRepository, "test")
        }

        afterSpec {
            noticeRepository.deleteAll()
            userRepository.deleteAll()
        }

        Given("간단한 공지사항을 생성하려고 할 때") {
            val noticeDto = CreateNoticeReq(
                title = "title",
                titleForMain = null,
                description = """
                            <h1>Hello, World!</h1>
                            <p>This is a test notice.</p>
                            <h3>Goodbye, World!</h3>
                """.trimIndent(),
                tags = emptyList(),
                isPrivate = false,
                isPinned = false,
                pinnedUntil = null,
                isImportant = false,
                importantUntil = null
            )

            When("공지사항을 생성하면") {
                val createdNoticeDto = noticeService.createNotice(noticeDto, null)

                Then("새 공지사항이 잘 생성되어야 한다.") {
                    noticeRepository.count() shouldBe 1
                    noticeRepository.findByIdOrNull(createdNoticeDto.id) shouldNotBe null
                }
                Then("plainTextDescription이 잘 생성되어야 한다.") {
                    val noticeEntity = noticeRepository.findByIdOrNull(createdNoticeDto.id)
                    noticeEntity?.plainTextDescription shouldBe "Hello, World! This is a test notice. Goodbye, World!"
                }
            }
        }

        Given("기존 간단한 공지사항의 Description을 수정하려고 할 때") {
            val noticeEntity = noticeRepository.save(
                NoticeEntity(
                    title = "title",
                    titleForMain = null,
                    description = """
                                    <h1>Hello, World!</h1>
                                    <p>This is a test notice.</p>
                                    <h3>Goodbye, World!</h3>
                    """.trimIndent(),
                    plainTextDescription = "Hello, World! This is a test notice. Goodbye, World!",
                    isPrivate = false,
                    isPinned = false,
                    isImportant = false,
                    author = userService.getLoginUser()
                )
            )
            val modifiedRequest = UpdateNoticeReq(
                title = noticeEntity.title,
                titleForMain = noticeEntity.titleForMain,
                description = """
                            <h1>Hello, World!</h1>
                            <p>This is a modified test notice.</p>
                            <h3>Goodbye, World!</h3>
                            <p>And this is a new line.</p>
                """.trimIndent(),
                isPrivate = false,
                isPinned = false,
                pinnedUntil = null,
                isImportant = false,
                importantUntil = null,
                tags = emptyList(),
                attachmentIds = emptyList()
            )

            When("수정된 DTO를 이용하여 수정하면") {
                val modifiedNoticeDto = noticeService.updateNotice(
                    noticeEntity.id,
                    modifiedRequest,
                    null
                )

                Then("plainTextDescription이 잘 수정되어야 한다.") {
                    val noticeEntity = noticeRepository.findByIdOrNull(modifiedNoticeDto.id)
                    noticeEntity?.plainTextDescription shouldBe (
                        "Hello, World! This is a modified test notice. Goodbye, World! And this is a new line."
                        )
                }
            }
        }
    }
}
