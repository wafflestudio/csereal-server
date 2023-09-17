package com.wafflestudio.csereal.core.notice.service

import com.wafflestudio.csereal.core.notice.database.NoticeEntity
import com.wafflestudio.csereal.core.notice.database.NoticeRepository
import com.wafflestudio.csereal.core.notice.dto.NoticeDto
import com.wafflestudio.csereal.core.user.database.Role
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

@SpringBootTest
class NoticeServiceTest(
    private val noticeService: NoticeService,
    private val userRepository: UserRepository,
    private val noticeRepository: NoticeRepository,
) : BehaviorSpec() {
    init {
        beforeContainer {
            userRepository.save(
                UserEntity(
                    "username",
                    "name",
                    "email",
                    "studentId",
                    Role.ROLE_STAFF
                )
            )
        }

        afterContainer {
            noticeRepository.deleteAll()
            userRepository.deleteAll()
        }

        Given("간단한 공지사항을 생성하려고 할 때") {
            val userEntity = userRepository.findByUsername("username")!!

            mockkStatic(RequestContextHolder::class)
            val mockRequestAttributes = mockk<RequestAttributes>()
            every {
                RequestContextHolder.getRequestAttributes()
            } returns mockRequestAttributes
            every {
                mockRequestAttributes.getAttribute(
                    "loggedInUser",
                    RequestAttributes.SCOPE_REQUEST
                )
            } returns userEntity

            val noticeDto = NoticeDto(
                id = -1,
                title = "title",
                titleForMain = null,
                description = """
                            <h1>Hello, World!</h1>
                            <p>This is a test notice.</p>
                            <h3>Goodbye, World!</h3>
                        """.trimIndent(),
                author = "username",
                tags = emptyList(),
                createdAt = null,
                modifiedAt = null,
                isPrivate = false,
                isPinned = false,
                isImportant = false,
                prevId = null,
                prevTitle = null,
                nextId = null,
                nextTitle = null,
                attachments = null,
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
                    author = userRepository.findByUsername("username")!!,
                )
            )
            val modifiedRequest = NoticeDto.of(
                noticeEntity, emptyList(), null
            ).copy(
                description = """
                            <h1>Hello, World!</h1>
                            <p>This is a modified test notice.</p>
                            <h3>Goodbye, World!</h3>
                            <p>And this is a new line.</p>
                        """.trimIndent()
            )

            When("수정된 DTO를 이용하여 수정하면") {
                val modifiedNoticeDto = noticeService.updateNotice(
                    modifiedRequest.id,
                    modifiedRequest,
                    null,
                    emptyList()
                )

                Then("plainTextDescription이 잘 수정되어야 한다.") {
                    val noticeEntity = noticeRepository.findByIdOrNull(modifiedNoticeDto.id)
                    noticeEntity?.plainTextDescription shouldBe "Hello, World! This is a modified test notice. Goodbye, World! And this is a new line."
                }
            }
        }
    }
}
