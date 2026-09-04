package com.wafflestudio.csereal.core.resource.attachment

import com.wafflestudio.csereal.core.about.database.AboutEntity
import com.wafflestudio.csereal.core.about.database.AboutPostType
import com.wafflestudio.csereal.core.about.database.AboutRepository
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentRepository
import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import com.wafflestudio.csereal.global.config.MySQLTestContainerConfig
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.io.File

/**
 * syncAttachments 의 계약을 고정한다 — attachmentIds 는 "남길 기존 첨부의 최종 목록".
 * 커밋 후 파일이 실제로 지워지는지도 봐야 하므로 @Transactional 롤백 테스트가 아니라
 * TransactionTemplate 으로 진짜 커밋한다(AFTER_COMMIT 리스너는 커밋돼야 돈다).
 */
@ActiveProfiles("test")
@SpringBootTest(properties = ["csereal.upload.path=build/tmp/attachment-test-uploads/"])
@Import(MySQLTestContainerConfig::class)
class AttachmentServiceTest(
    private val attachmentService: AttachmentService,
    private val attachmentRepository: AttachmentRepository,
    private val aboutRepository: AboutRepository,
    transactionManager: PlatformTransactionManager
) : BehaviorSpec() {
    private val tx = TransactionTemplate(transactionManager)
    private val uploadDir = File("build/tmp/attachment-test-uploads")

    private fun file(name: String) = MockMultipartFile("attachments", name, "text/plain", name.toByteArray())

    /** 첨부 [names]를 가진 about 글을 커밋하고 id를 돌려준다. */
    private fun createOwnerWith(vararg names: String): Long = tx.execute {
        // 첨부는 콘텐츠(부모)에 붙는다 — 번역본은 이 테스트와 무관해 만들지 않는다.
        val about = aboutRepository.save(AboutEntity(postType = AboutPostType.OVERVIEW))
        attachmentService.uploadAllAttachments(about, names.map(::file))
        about.id
    }!!

    private fun namesOf(ownerId: Long): List<String> = tx.execute {
        aboutRepository.findById(ownerId).get().attachments.map { it.filename.substringAfter("_") }
    }!!

    private fun filesOnDisk(): Set<String> =
        uploadDir.listFiles().orEmpty().map { it.name.substringAfter("_") }.toSet()

    init {
        afterSpec {
            attachmentRepository.deleteAll()
            aboutRepository.deleteAll()
            uploadDir.deleteRecursively()
        }

        Given("첨부 a·b·c 를 가진 글이 있을 때") {
            When("attachmentIds 에 a·c 만 남기면") {
                val id = createOwnerWith("a", "b", "c")
                val keep = tx.execute {
                    aboutRepository.findById(id).get().attachments.filter {
                        it.filename.endsWith("_a") || it.filename.endsWith(
                            "_c"
                        )
                    }.map { it.id }
                }!!
                tx.executeWithoutResult {
                    attachmentService.syncAttachments(aboutRepository.findById(id).get(), keep, null)
                }
                Then("b 만 행과 파일이 함께 사라진다") {
                    namesOf(id) shouldContainExactlyInAnyOrder listOf("a", "c")
                    filesOnDisk().contains("b") shouldBe false
                    filesOnDisk().containsAll(listOf("a", "c")) shouldBe true
                }
            }

            When("attachmentIds 가 빈 목록이면") {
                val id = createOwnerWith("a", "b")
                tx.executeWithoutResult {
                    attachmentService.syncAttachments(
                        aboutRepository.findById(id).get(),
                        emptyList(),
                        null
                    )
                }
                Then("전부 지운다") {
                    namesOf(id) shouldBe emptyList()
                }
            }

            When("attachmentIds 가 null 이면") {
                val id = createOwnerWith("a", "b")
                tx.executeWithoutResult {
                    attachmentService.syncAttachments(
                        aboutRepository.findById(id).get(),
                        null,
                        null
                    )
                }
                Then("기존 첨부를 건드리지 않는다") {
                    namesOf(id) shouldContainExactlyInAnyOrder listOf("a", "b")
                }
            }

            When("남길 목록과 새 파일을 함께 주면") {
                val id = createOwnerWith("a", "b")
                val keepA = tx.execute {
                    aboutRepository.findById(id).get().attachments.filter {
                        it.filename.endsWith(
                            "_a"
                        )
                    }.map { it.id }
                }!!
                tx.executeWithoutResult {
                    attachmentService.syncAttachments(aboutRepository.findById(id).get(), keepA, listOf(file("d")))
                }
                Then("b 는 지우고 d 는 올려 a·d 가 된다") {
                    namesOf(id) shouldContainExactlyInAnyOrder listOf("a", "d")
                    filesOnDisk().contains("d") shouldBe true
                }
            }
        }
    }
}
