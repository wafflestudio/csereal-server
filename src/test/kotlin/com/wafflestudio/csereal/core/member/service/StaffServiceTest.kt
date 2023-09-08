package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.core.member.database.MemberSearchRepository
import com.wafflestudio.csereal.core.member.database.StaffRepository
import com.wafflestudio.csereal.core.member.dto.StaffDto
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.transaction.Transactional
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest
@Transactional
class StaffServiceTest(
        private val staffService: StaffService,
        private val staffRepository: StaffRepository,
        private val memberSearchRepository: MemberSearchRepository,
): BehaviorSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    afterSpec {
        staffRepository.deleteAll()
    }

    Given("이미지 없는 행정직원을 생성하려고 할 떄") {
        val staffDto = StaffDto(
                name = "name",
                role = "role",
                office = "office",
                phone = "phone",
                email = "email",
                tasks = listOf("task1", "task2"),
        )

        When("행정직원을 생성하면") {
            val createdStaffDto = staffService.createStaff(staffDto, null)

            Then("행정직원이 생성된다") {
                staffRepository.count() shouldBe 1
                staffRepository.findByIdOrNull(createdStaffDto.id!!) shouldNotBe null
            }

            Then("행정직원의 정보가 일치한다") {
                val staffEntity = staffRepository.findByIdOrNull(createdStaffDto.id!!)!!
                staffEntity.name shouldBe staffDto.name
                staffEntity.role shouldBe staffDto.role
                staffEntity.office shouldBe staffDto.office
                staffEntity.phone shouldBe staffDto.phone
                staffEntity.email shouldBe staffDto.email
                staffEntity.tasks.map { it.name } shouldBe staffDto.tasks
            }

            Then("검색 정보가 생성된다") {
                memberSearchRepository.count() shouldBe 1

                val staffEntity = staffRepository.findByIdOrNull(createdStaffDto.id!!)!!
                val memberSearch = staffEntity.memberSearch!!

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
        val staffDto = StaffDto(
                name = "name",
                role = "role",
                office = "office",
                phone = "phone",
                email = "email",
                tasks = listOf("task1", "task2"),
        )

        val createdStaffDto = staffService.createStaff(staffDto, null)

        When("행정직원을 수정하면") {
            val updateStaffDto = StaffDto(
                    name = "name2",
                    role = "role2",
                    office = "office2",
                    phone = "phone2",
                    email = "email2",
                    tasks = listOf("task1", "task3", "task4"),
            )

            val updatedStaffDto = staffService.updateStaff(createdStaffDto.id!!, updateStaffDto, null)

            Then("행정직원의 정보가 일치한다") {
                staffRepository.count() shouldBe 1
                val staffEntity = staffRepository.findByIdOrNull(updatedStaffDto.id!!)!!
                staffEntity.name shouldBe updateStaffDto.name
                staffEntity.role shouldBe updateStaffDto.role
                staffEntity.office shouldBe updateStaffDto.office
                staffEntity.phone shouldBe updateStaffDto.phone
                staffEntity.email shouldBe updateStaffDto.email
                staffEntity.tasks.map { it.name } shouldBe updateStaffDto.tasks
            }

            Then("검색 정보가 수정된다") {
                memberSearchRepository.count() shouldBe 1

                val staffEntity = staffRepository.findByIdOrNull(updatedStaffDto.id!!)!!
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