package com.wafflestudio.csereal.core.seminar.service

import com.wafflestudio.csereal.core.seminar.database.SeminarEntity
import com.wafflestudio.csereal.core.seminar.database.SeminarRepository
import com.wafflestudio.csereal.core.seminar.dto.SeminarDto
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.transaction.Transactional
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime

@SpringBootTest
@Transactional
class SeminarServiceTest(
    private val seminarService: SeminarService,
    private val seminarRepository: SeminarRepository,
) : BehaviorSpec() {
    init {

        beforeContainer {
        }

        afterContainer {
            seminarRepository.deleteAll()
        }

        Given("세미나를 생성하려고 할 때") {
            val seminarDTO = SeminarDto(
                id = -1,
                title = "title",
                description = """
                        <h1>Hello, World!</h1>
                        <p>This is seminar description.</p>
                        <h3>Goodbye, World!</h3>
                        """.trimIndent(),
                introduction = """
                        <h1>Hello, World!</h1>
                        <p>This is seminar introduction.</p>
                        <h3>Goodbye, World!</h3>
                        """.trimIndent(),
                name = "name",
                speakerURL = "speakerURL",
                speakerTitle = "speakerTitle",
                affiliation = "affiliation",
                affiliationURL = "affiliationURL",
                startDate = LocalDateTime.now(),
                endDate = LocalDateTime.now(),
                location = "location",
                host = "host",
                additionalNote = """
                            <h1>Hello, World!</h1>
                            <p>This is seminar additionalNote.</p>
                            <h3>Goodbye, World!</h3>
                        """.trimIndent(),
                createdAt = null,
                modifiedAt = null,
                isPrivate = false,
                isImportant = false,
                prevId = null,
                prevTitle = null,
                nextId = null,
                nextTitle = null,
                imageURL = null,
                attachments = null
            )
            When("간단한 세미나 DTO가 주어지면") {
                val resultSeminarDTO = seminarService.createSeminar(seminarDTO, null, null)

                Then("세미나가 생성된다") {
                    seminarRepository.count() shouldBe 1
                    seminarRepository.findByIdOrNull(resultSeminarDTO.id) shouldNotBe null
                }

                Then("plain text 값들이 잘 생성되어야 한다.") {
                    val seminarEntity = seminarRepository.findByIdOrNull(resultSeminarDTO.id)!!
                    seminarEntity.plainTextDescription shouldBe "Hello, World! This is seminar description. Goodbye, World!"
                    seminarEntity.plainTextIntroduction shouldBe "Hello, World! This is seminar introduction. Goodbye, World!"
                    seminarEntity.plainTextAdditionalNote shouldBe "Hello, World! This is seminar additionalNote. Goodbye, World!"
                }
            }
        }

        Given("기존 간단한 세미나의 Description을 수정하려고 할 때") {
            val originalSeminar = seminarRepository.save(
                SeminarEntity(
                    title = "title",
                    description = """
                                <h1>Hello, World!</h1>
                                <p>This is seminar description.</p>
                                <h3>Goodbye, World!</h3>
                                """.trimIndent(),
                    plainTextDescription = "Hello, World! This is seminar description. Goodbye, World!",
                    introduction = """
                                <h1>Hello, World!</h1>
                                <p>This is seminar introduction.</p>
                                <h3>Goodbye, World!</h3>
                                """.trimIndent(),
                    plainTextIntroduction = "Hello, World! This is seminar introduction. Goodbye, World!",
                    name = "name",
                    speakerURL = "speakerURL",
                    speakerTitle = "speakerTitle",
                    affiliation = "affiliation",
                    affiliationURL = "affiliationURL",
                    startDate = LocalDateTime.now(),
                    endDate = LocalDateTime.now(),
                    location = "location",
                    host = "host",
                    additionalNote = """
                                    <h1>Hello, World!</h1>
                                    <p>This is seminar additionalNote.</p>
                                    <h3>Goodbye, World!</h3>
                                """.trimIndent(),
                    plainTextAdditionalNote = "Hello, World! This is seminar additionalNote. Goodbye, World!",
                    isPrivate = false,
                    isImportant = false,
                )
            )
            val originalId = originalSeminar.id

            When("수정된 DTO를 이용하여 수정하면") {
                val modifiedSeminarDTO = SeminarDto.of(
                    originalSeminar, null, emptyList(), null
                ).copy(
                    description = """
                                <h1>Hello, World!</h1>
                                <p>This is modified seminar description.</p>
                                <h3>Goodbye, World!</h3>
                                <p>And this is a new line.</p>
                            """.trimIndent(),
                    introduction = """
                                <h1>Hello, World!</h1>
                                <p>This is modified seminar introduction.</p>
                                <h3>Goodbye, World!</h3>
                                <p>And this is a new line.</p>
                            """.trimIndent(),
                    additionalNote = """
                                <h1>Hello, World!</h1>
                                <p>This is modified seminar additionalNote.</p>
                                <h3>Goodbye, World!</h3>
                                <p>And this is a new line.</p>
                            """.trimIndent(),
                )

                val modifiedSeminarDto = seminarService.updateSeminar(
                    originalSeminar.id,
                    modifiedSeminarDTO,
                    null,
                    null,
                )

                Then("같은 Entity가 수정되어야 한다.") {
                    seminarRepository.count() shouldBe 1
                    val modifiedSeminarEntity = seminarRepository.findByIdOrNull(modifiedSeminarDto.id)!!
                    modifiedSeminarEntity.id shouldBe originalId
                }

                Then("plain text 값들이 잘 수정되어야 한다.") {
                    val modifiedSeminarEntity = seminarRepository.findByIdOrNull(modifiedSeminarDto.id)!!
                    modifiedSeminarEntity.plainTextDescription shouldBe "Hello, World! This is modified seminar description. Goodbye, World! And this is a new line."
                    modifiedSeminarEntity.plainTextIntroduction shouldBe "Hello, World! This is modified seminar introduction. Goodbye, World! And this is a new line."
                    modifiedSeminarEntity.plainTextAdditionalNote shouldBe "Hello, World! This is modified seminar additionalNote. Goodbye, World! And this is a new line."
                }
            }
        }
    }
}
