package com.wafflestudio.csereal.core.internal.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.internal.database.InternalEntity
import com.wafflestudio.csereal.core.internal.database.InternalRepository
import com.wafflestudio.csereal.core.internal.dto.InternalDto
import com.wafflestudio.csereal.global.config.MySQLTestContainerConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@Import(MySQLTestContainerConfig::class)
class InternalServiceTest(
    private val internalService: InternalService,
    private val internalRepository: InternalRepository
) : BehaviorSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    Given("Internal이 비어있을 때") {
        When("Get을 실행하면") {
            Then("400 에러를 발생시켜야 한다.") {
                val exc: CserealException.Csereal400 = shouldThrow {
                    internalService.getInternal()
                }
            }
        }
        When("Modify를 실행하면") {
            val desc = "Test"
            val modifyDto = InternalDto(desc)
            val returnDto = internalService.modifyInternal(modifyDto)
            Then("새로운 entity를 생성해야 한다.") {
                internalRepository.count() shouldBe 1L
            }
            Then("생성된 entity의 설명이 일치해야 한다.") {
                internalRepository.findFirstByOrderByModifiedAtDesc().description shouldBe desc
            }
        }
    }

    Given("Internal이 하나 있는 경우 (정상적인 상황)") {
        val desc = "<p>Hello</p>"
        val originalEntity = internalRepository.save(InternalEntity(desc))
        When("Get을 실행하면") {
            val dto = internalService.getInternal()
            Then("해당 dto가 반환되어야 한다.") {
                dto shouldBe InternalDto(desc)
            }
        }
        When("Modify를 실행하면") {
            val modDesc = "<p>Bye</p>"
            val modDto = InternalDto(modDesc)
            val returnDto = internalService.modifyInternal(modDto)

            Then("기존 entity의 설명이 변경되어야 한다.") {
                internalRepository.findByIdOrNull(originalEntity.id)!!.description shouldBe modDesc
            }
            Then("변경된 entity의 설명이 반환되어야 한다.") {
                returnDto shouldBe InternalDto(modDesc)
            }
        }
    }

    Given("Internal이 여러개 있는 경우") {
        val desc = "<p>Hello</p>"
        val originalEntity = internalRepository.save(InternalEntity(desc))
        val desc2 = "<p>Bye</p>"
        val originalEntity2 = internalRepository.save(InternalEntity(desc2))
        val largestId = maxOf(originalEntity.id, originalEntity2.id)

        When("Get을 실행하면") {
            val dto = internalService.getInternal()
            Then("가장 최근 entity의 설명이 반환되어야 한다.") {
                dto shouldBe InternalDto(desc2)
            }
        }

        When("Modify를 실행하면") {
            val modDesc = "<p>BBBBB</p>"
            val modDto = InternalDto(modDesc)
            val returnDto = internalService.modifyInternal(modDto)

            Then("기존 entity들이 모두 삭제되어야 한다.") {
                internalRepository.findByIdOrNull(originalEntity.id) shouldBe null
                internalRepository.findByIdOrNull(originalEntity2.id) shouldBe null
                internalRepository.count() shouldBe 1L
            }
            Then("새로운 entity가 생성되어야 한다.") {
                internalRepository.findFirstByOrderByModifiedAtDesc().let {
                    it.id shouldBeGreaterThan largestId
                    it.description shouldBe modDesc
                }
            }
            Then("변경된 entity의 설명이 반환되어야 한다.") {
                returnDto shouldBe InternalDto(modDesc)
            }
        }
    }
})
