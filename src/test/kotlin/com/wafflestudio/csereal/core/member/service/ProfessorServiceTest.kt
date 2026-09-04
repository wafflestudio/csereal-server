package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.api.req.CreateProfessorLanguagesReqBody
import com.wafflestudio.csereal.core.member.api.req.CreateProfessorReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyProfessorLanguagesReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyProfessorReqBody
import com.wafflestudio.csereal.core.member.database.MemberSearchRepository
import com.wafflestudio.csereal.core.member.database.ProfessorRepository
import com.wafflestudio.csereal.core.member.database.ProfessorStatus
import com.wafflestudio.csereal.core.research.database.LabEntity
import com.wafflestudio.csereal.core.research.database.LabRepository
import com.wafflestudio.csereal.core.research.database.LabTranslationEntity
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
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(MySQLTestContainerConfig::class)
class ProfessorServiceTest(
    private val professorService: ProfessorService,
    private val professorRepository: ProfessorRepository,
    private val labRepository: LabRepository,
    private val memberSearchRepository: MemberSearchRepository
) : BehaviorSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    afterContainer {
        professorRepository.deleteAll()
        labRepository.deleteAll()
    }

    fun saveLab(koName: String, enName: String): LabEntity {
        val lab = LabEntity()
        lab.translations.add(LabTranslationEntity(lab, LanguageType.KO, koName))
        lab.translations.add(LabTranslationEntity(lab, LanguageType.EN, enName))
        return labRepository.save(lab)
    }

    Given("이미지 없는 교수를 생성하려고 할 때") {
        val date = LocalDate.now()
        val lab = saveLab("연구실", "labName")

        val createReq = CreateProfessorLanguagesReqBody(
            status = ProfessorStatus.ACTIVE,
            labId = lab.id,
            startDate = date,
            endDate = date,
            phone = "phone",
            fax = "fax",
            email = "email",
            website = "website",
            ko = CreateProfessorReqBody(
                name = "이름",
                academicRank = "교수",
                department = "컴퓨터공학부",
                office = "office",
                educations = listOf("학력1", "학력2 "),
                researchAreas = listOf("분야1", "분야2 "),
                careers = listOf("경력1", "경력2 ")
            ),
            en = CreateProfessorReqBody(
                name = "name",
                academicRank = "academicRank",
                department = "department",
                office = "office",
                educations = listOf("education1", "education2"),
                researchAreas = listOf("researchArea1", "researchArea2"),
                careers = listOf("career1", "career2")
            )
        )

        When("교수를 생성한다면") {
            val created = professorService.createProfessorLanguages(createReq, null)

            Then("교수 한 명에 번역본 두 개가 생긴다") {
                professorRepository.count() shouldBe 1
                professorRepository.findByIdOrNull(created.id) shouldNotBe null
                created.ko shouldNotBe null
                created.en shouldNotBe null
            }

            Then("언어 무관 값은 응답 최상위에 한 벌만 있다") {
                val professor = professorRepository.findByIdOrNull(created.id)!!
                professor.status shouldBe ProfessorStatus.ACTIVE
                professor.lab?.id shouldBe lab.id
                professor.startDate shouldBe date
                professor.email shouldBe "email"
                created.phone shouldBe "phone"
                created.labId shouldBe lab.id
            }

            Then("언어별 값과 연구실 이름은 각 언어판을 따른다") {
                created.ko!!.name shouldBe "이름"
                created.ko!!.labName shouldBe "연구실"
                created.en!!.name shouldBe "name"
                created.en!!.labName shouldBe "labName"
                created.ko!!.educations shouldBe listOf("학력1", "학력2")
                // 호실은 언어별 값이라 ko/en 안에 있다.
                created.ko!!.office shouldBe "office"
            }

            Then("검색 색인이 언어마다 하나씩 생긴다") {
                memberSearchRepository.count() shouldBe 2
                val professor = professorRepository.findByIdOrNull(created.id)!!
                val enSearch = professor.translationOf(LanguageType.EN)!!.memberSearch!!
                enSearch.language shouldBe LanguageType.EN
                enSearch.content shouldBe """
                    name
                    교수
                    academicRank
                    department
                    labName
                    $date
                    $date
                    office
                    phone
                    fax
                    email
                    website
                    education1
                    education2
                    researchArea1
                    researchArea2
                    career1
                    career2
                    
                """.trimIndent()
            }
        }
    }

    Given("생성되어 있는 교수에 대하여") {
        val date = LocalDate.now()
        val lab1 = saveLab("연구실1", "lab1")
        val lab2 = saveLab("연구실2", "lab2")

        val created = professorService.createProfessorLanguages(
            CreateProfessorLanguagesReqBody(
                status = ProfessorStatus.ACTIVE,
                labId = lab1.id,
                startDate = date,
                endDate = date,
                phone = "phone",
                fax = "fax",
                email = "email",
                website = "website",
                ko = CreateProfessorReqBody("이름", "교수", "컴퓨터공학부", "office", listOf(), listOf(), listOf()),
                en = CreateProfessorReqBody(
                    "name",
                    "academicRank",
                    "department",
                    "office",
                    listOf(),
                    listOf(),
                    listOf()
                )
            ),
            null
        )

        When("교수 정보를 수정하면") {
            val modifyReq = ModifyProfessorLanguagesReqBody(
                status = ProfessorStatus.INACTIVE,
                labId = lab2.id,
                startDate = date,
                endDate = date,
                phone = "phone2",
                fax = "fax2",
                email = "email2",
                website = "website2",
                removeImage = false,
                ko = ModifyProfessorReqBody("이름2", "교수2", "학부2", "office2", listOf("학력1"), listOf(), listOf()),
                en = ModifyProfessorReqBody(
                    "name2",
                    "rank2",
                    "dept2",
                    "office2",
                    listOf("education1"),
                    listOf(),
                    listOf()
                )
            )
            val modified = professorService.updateProfessorLanguages(created.id, modifyReq, null)

            Then("교수는 그대로 하나고 값이 바뀐다") {
                professorRepository.count() shouldBe 1
                val professor = professorRepository.findByIdOrNull(modified.id)!!
                professor.status shouldBe ProfessorStatus.INACTIVE
                professor.lab?.id shouldBe lab2.id
                professor.translationOf(LanguageType.KO)!!.office shouldBe "office2"
                professor.translationOf(LanguageType.KO)!!.name shouldBe "이름2"
                professor.translationOf(LanguageType.EN)!!.name shouldBe "name2"
            }

            Then("검색 색인도 언어마다 갱신된다") {
                memberSearchRepository.count() shouldBe 2
                val professor = professorRepository.findByIdOrNull(modified.id)!!
                professor.translationOf(LanguageType.EN)!!.memberSearch!!.content shouldBe """
                    name2
                    역대 교수
                    rank2
                    dept2
                    lab2
                    $date
                    $date
                    office2
                    phone2
                    fax2
                    email2
                    website2
                    education1
                    
                """.trimIndent()
            }
        }
    }
})
