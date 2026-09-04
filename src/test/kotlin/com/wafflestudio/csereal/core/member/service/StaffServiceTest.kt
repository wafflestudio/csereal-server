package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.api.req.CreateStaffLanguagesReqBody
import com.wafflestudio.csereal.core.member.api.req.CreateStaffReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyStaffLanguagesReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyStaffReqBody
import com.wafflestudio.csereal.core.member.database.MemberSearchRepository
import com.wafflestudio.csereal.core.member.database.StaffRepository
import com.wafflestudio.csereal.core.member.database.StaffTranslationRepository
import com.wafflestudio.csereal.global.config.MySQLTestContainerConfig
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.transaction.Transactional
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(MySQLTestContainerConfig::class)
class StaffServiceTest(
    private val staffService: StaffService,
    private val staffRepository: StaffRepository,
    private val staffTranslationRepository: StaffTranslationRepository,
    private val memberSearchRepository: MemberSearchRepository
) : BehaviorSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    afterSpec {
        staffRepository.deleteAll()
    }

    Given("이미지 없는 행정직원을 생성하려고 할 때") {
        val createReq = CreateStaffLanguagesReqBody(
            phone = "phone",
            email = "email",
            ko = CreateStaffReqBody(name = "이름", role = "역할", office = "office", tasks = listOf("업무1", "업무2 ")),
            en = CreateStaffReqBody(name = "name", role = "role", office = "office", tasks = listOf("task1", "task2"))
        )

        When("행정직원을 생성하면") {
            val created = staffService.createStaffLanguages(createReq, null)

            Then("직원 한 명에 번역본 두 개가 생긴다") {
                staffRepository.count() shouldBe 1
                staffTranslationRepository.count() shouldBe 2
                staffRepository.findByIdOrNull(created.id) shouldNotBe null
            }

            Then("한/영이 한 직원 아래에 있다") {
                created.ko shouldNotBe null
                created.en shouldNotBe null
            }

            Then("연락처는 응답 최상위에 한 벌만 있다") {
                created.phone shouldBe "phone"
                created.email shouldBe "email"
            }

            Then("언어별 값은 각자 다르다") {
                created.ko!!.name shouldBe "이름"
                created.en!!.name shouldBe "name"
                created.ko!!.tasks shouldBe listOf("업무1", "업무2")
                // 호실은 언어별 값이라 ko/en 안에 있다.
                created.ko!!.office shouldBe "office"
            }

            Then("검색 색인이 언어마다 하나씩 생긴다") {
                memberSearchRepository.count() shouldBe 2
                val staff = staffRepository.findByIdOrNull(created.id)!!
                val koSearch = staff.translationOf(LanguageType.KO)!!.memberSearch!!
                koSearch.language shouldBe LanguageType.KO
                koSearch.content shouldBe """
                    이름
                    역할
                    office
                    phone
                    email
                    업무1
                    업무2
                    
                """.trimIndent()
            }
        }
    }

    Given("이미지 없는 행정직원을 수정할 때") {
        val created = staffService.createStaffLanguages(
            CreateStaffLanguagesReqBody(
                phone = "phone",
                email = "email",
                ko = CreateStaffReqBody(name = "이름", role = "역할", office = "office", tasks = listOf("업무1", "업무2")),
                en = CreateStaffReqBody(
                    name = "name",
                    role = "role",
                    office = "office",
                    tasks = listOf("task1", "task2")
                )
            ),
            null
        )

        When("행정직원을 수정하면") {
            val modifyReq = ModifyStaffLanguagesReqBody(
                phone = "phone2",
                email = "email2",
                removeImage = false,
                ko = ModifyStaffReqBody(name = "이름2", role = "역할2", office = "office2", tasks = listOf("업무1", "업무3 ")),
                en = ModifyStaffReqBody(
                    name = "name2",
                    role = "role2",
                    office = "office2",
                    tasks = listOf("task1", "task3")
                )
            )
            val updated = staffService.updateStaffLanguages(created.id, modifyReq, null)

            Then("직원은 그대로 하나고 값이 바뀐다") {
                staffRepository.count() shouldBe 1
                val staff = staffRepository.findByIdOrNull(updated.id)!!
                staff.translationOf(LanguageType.KO)!!.office shouldBe "office2"
                staff.translationOf(LanguageType.KO)!!.name shouldBe "이름2"
                staff.translationOf(LanguageType.EN)!!.name shouldBe "name2"
                staff.translationOf(LanguageType.KO)!!.tasks shouldBe listOf("업무1", "업무3")
            }

            Then("검색 색인도 언어마다 갱신된다") {
                memberSearchRepository.count() shouldBe 2
                val staff = staffRepository.findByIdOrNull(updated.id)!!
                staff.translationOf(LanguageType.KO)!!.memberSearch!!.content shouldBe """
                    이름2
                    역할2
                    office2
                    phone2
                    email2
                    업무1
                    업무3
                    
                """.trimIndent()
            }
        }
    }
})
