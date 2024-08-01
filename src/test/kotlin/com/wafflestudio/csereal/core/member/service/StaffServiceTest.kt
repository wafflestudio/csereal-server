package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.api.req.CreateStaffReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyStaffReqBody
import com.wafflestudio.csereal.core.member.database.MemberSearchRepository
import com.wafflestudio.csereal.core.member.database.StaffRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.transaction.Transactional
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StaffServiceTest(
    private val staffService: StaffService,
    private val staffRepository: StaffRepository,
    private val memberSearchRepository: MemberSearchRepository
) : BehaviorSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    afterSpec {
        staffRepository.deleteAll()
    }

    // TODO: staff 쌍으로 묶은 테스트 생성

    Given("이미지 없는 행정직원을 생성하려고 할 떄") {
        val createStaffReq = CreateStaffReqBody(
            name = "name",
            role = "role",
            office = "office",
            phone = "phone",
            email = "email",
            tasks = listOf("task1", "task2")
        )

        When("행정직원을 생성하면") {
            val createdStaffDto = staffService.createStaff(LanguageType.KO, createStaffReq, null)

            Then("행정직원이 생성된다") {
                staffRepository.count() shouldBe 1
                staffRepository.findByIdOrNull(createdStaffDto.id) shouldNotBe null
            }

            Then("행정직원의 정보가 일치한다") {
                val staffEntity = staffRepository.findByIdOrNull(createdStaffDto.id)!!
                staffEntity.name shouldBe createStaffReq.name
                staffEntity.role shouldBe createStaffReq.role
                staffEntity.office shouldBe createStaffReq.office
                staffEntity.phone shouldBe createStaffReq.phone
                staffEntity.email shouldBe createStaffReq.email
                staffEntity.tasks.map { it.name } shouldBe createStaffReq.tasks
            }

            Then("검색 정보가 생성된다") {
                memberSearchRepository.count() shouldBe 1

                val staffEntity = staffRepository.findByIdOrNull(createdStaffDto.id)!!
                val memberSearch = staffEntity.memberSearch!!

                memberSearch.language shouldBe LanguageType.KO
                memberSearch.content shouldBe """
                    name
                    role
                    office
                    phone
                    email
                    task1
                    task2
                    
                """.trimIndent()
            }
        }
    }

    Given("이미지 없는 행정직원을 수정할 때") {
        val createStaffReq = CreateStaffReqBody(
            name = "name",
            role = "role",
            office = "office",
            phone = "phone",
            email = "email",
            tasks = listOf("task1", "task2")
        )
        val createdStaffDto = staffService.createStaff(LanguageType.KO, createStaffReq, null)

        When("행정직원을 수정하면") {
            val modifyStaffReq = ModifyStaffReqBody(
                name = "name2",
                role = "role2",
                office = "office2",
                phone = "phone2",
                email = "email2",
                tasks = listOf("task1", "task3", "task4"),
                removeImage = false
            )

            val updatedStaffDto = staffService.updateStaff(createdStaffDto.id, modifyStaffReq, null)

            Then("행정직원의 정보가 일치한다") {
                staffRepository.count() shouldBe 1
                val staffEntity = staffRepository.findByIdOrNull(updatedStaffDto.id)!!
                staffEntity.name shouldBe modifyStaffReq.name
                staffEntity.role shouldBe modifyStaffReq.role
                staffEntity.office shouldBe modifyStaffReq.office
                staffEntity.phone shouldBe modifyStaffReq.phone
                staffEntity.email shouldBe modifyStaffReq.email
                staffEntity.tasks.map { it.name } shouldBe modifyStaffReq.tasks
            }

            Then("검색 정보가 수정된다") {
                memberSearchRepository.count() shouldBe 1

                val staffEntity = staffRepository.findByIdOrNull(updatedStaffDto.id)!!
                val memberSearch = staffEntity.memberSearch!!

                memberSearch.content shouldBe """
                    name2
                    role2
                    office2
                    phone2
                    email2
                    task1
                    task3
                    task4
                    
                """.trimIndent()
            }
        }
    }
})
