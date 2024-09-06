package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.api.req.CreateProfessorReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyProfessorReqBody
import com.wafflestudio.csereal.core.member.database.MemberSearchRepository
import com.wafflestudio.csereal.core.member.database.ProfessorRepository
import com.wafflestudio.csereal.core.member.database.ProfessorStatus
import com.wafflestudio.csereal.core.research.database.LabEntity
import com.wafflestudio.csereal.core.research.database.LabRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.transaction.Transactional
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@Transactional
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

    Given("이미지 없는 교수를 생성하려고 할 때") {
        val date = LocalDate.now()

        var labEntity = LabEntity(
            language = LanguageType.KO,
            name = "labName",
            location = null,
            tel = null,
            acronym = null,
            youtube = null,
            description = null,
            websiteURL = null
        )
        labEntity = labRepository.save(labEntity)

        val professorCreateReq = CreateProfessorReqBody(
            name = "name",
            email = "email",
            status = ProfessorStatus.ACTIVE,
            academicRank = "academicRank",
            labId = labEntity.id,
            startDate = date,
            endDate = date,
            office = "office",
            phone = "phone",
            fax = "fax",
            website = "website",
            educations = listOf("education1", "education2"),
            researchAreas = listOf("researchArea1", "researchArea2"),
            careers = listOf("career1", "career2")
        )

        When("교수를 생성한다면") {
            val createdProfessorDto = professorService.createProfessor(LanguageType.KO, professorCreateReq, null)

            Then("교수가 생성되어야 한다") {
                professorRepository.count() shouldBe 1
                professorRepository.findByIdOrNull(createdProfessorDto.id) shouldNotBe null
            }

            Then("교수의 정보가 일치해야 한다") {
                val professorEntity = professorRepository.findByIdOrNull(createdProfessorDto.id)!!

                professorEntity.name shouldBe professorCreateReq.name
                professorEntity.email shouldBe professorCreateReq.email
                professorEntity.status shouldBe professorCreateReq.status
                professorEntity.academicRank shouldBe professorCreateReq.academicRank
                professorEntity.lab shouldBe labEntity
                professorEntity.startDate shouldBe professorCreateReq.startDate
                professorEntity.endDate shouldBe professorCreateReq.endDate
                professorEntity.office shouldBe professorCreateReq.office
                professorEntity.phone shouldBe professorCreateReq.phone
                professorEntity.fax shouldBe professorCreateReq.fax
                professorEntity.website shouldBe professorCreateReq.website
                professorEntity.educations.map { it.name } shouldBe professorCreateReq.educations
                professorEntity.researchAreas.map { it.name } shouldBe professorCreateReq.researchAreas
                professorEntity.careers.map { it.name } shouldBe professorCreateReq.careers
            }

            Then("교수의 검색 정보가 생성되어야 한다") {
                memberSearchRepository.count() shouldBe 1
                val memberSearchEntity = memberSearchRepository.findAll()[0]

                memberSearchEntity.professor?.id shouldBe createdProfessorDto.id
                memberSearchEntity.language shouldBe LanguageType.KO

                val contentExpected = """
                        name
                        교수
                        academicRank
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

                memberSearchEntity.content shouldBe contentExpected
            }
        }
    }

    Given("생성되어 있는 간단한 교수에 대하여") {
        val date = LocalDate.now()
        val labEntity1 = labRepository.save(
            LabEntity(
                language = LanguageType.KO,
                name = "labName1",
                location = null,
                tel = null,
                acronym = null,
                youtube = null,
                description = null,
                websiteURL = null
            )
        )
        val labEntity2 = labRepository.save(
            LabEntity(
                language = LanguageType.KO,
                name = "labName2",
                location = null,
                tel = null,
                acronym = null,
                youtube = null,
                description = null,
                websiteURL = null
            )
        )

        val createdProfessorDto = professorService.createProfessor(
            LanguageType.KO,
            CreateProfessorReqBody(
                name = "name",
                email = "email",
                status = ProfessorStatus.ACTIVE,
                academicRank = "academicRank",
                labId = labEntity1.id,
                startDate = date,
                endDate = date,
                office = "office",
                phone = "phone",
                fax = "fax",
                website = "website",
                educations = listOf("education1", "education2"),
                researchAreas = listOf("researchArea1", "researchArea2"),
                careers = listOf("career1", "career2")
            ),
            null
        )

        When("교수 정보를 수정하면") {
            val modifyProfessorReq = ModifyProfessorReqBody(
                name = "modifiedName",
                email = "modifiedEmail",
                status = ProfessorStatus.INACTIVE,
                academicRank = "modifiedAcademicRank",
                labId = labEntity2.id,
                startDate = date.plusDays(1),
                endDate = date.plusDays(1),
                office = "modifiedOffice",
                phone = "modifiedPhone",
                fax = "modifiedFax",
                website = "modifiedWebsite",
                educations = listOf("education1", "modifiedEducation2", "modifiedEducation3"),
                researchAreas = listOf("researchArea1", "modifiedResearchArea2", "modifiedResearchArea3"),
                careers = listOf("career1", "modifiedCareer2", "modifiedCareer3"),
                removeImage = false
            )

            val modifiedProfessorDto = professorService.updateProfessor(
                createdProfessorDto.id,
                modifyProfessorReq,
                null
            )

            Then("교수 정보가 수정되어야 한다.") {
                professorRepository.count() shouldBe 1
                val professorEntity = professorRepository.findByIdOrNull(modifiedProfessorDto.id)
                professorEntity shouldNotBe null

                professorEntity!!.name shouldBe modifyProfessorReq.name
                professorEntity.email shouldBe modifyProfessorReq.email
                professorEntity.status shouldBe modifyProfessorReq.status
                professorEntity.academicRank shouldBe modifyProfessorReq.academicRank
                professorEntity.lab shouldBe labEntity2
                professorEntity.startDate shouldBe modifyProfessorReq.startDate
                professorEntity.endDate shouldBe modifyProfessorReq.endDate
                professorEntity.office shouldBe modifyProfessorReq.office
                professorEntity.phone shouldBe modifyProfessorReq.phone
                professorEntity.fax shouldBe modifyProfessorReq.fax
                professorEntity.website shouldBe modifyProfessorReq.website
                professorEntity.educations.map { it.name } shouldBe modifyProfessorReq.educations
                professorEntity.researchAreas.map { it.name } shouldBe modifyProfessorReq.researchAreas
                professorEntity.careers.map { it.name } shouldBe modifyProfessorReq.careers
            }

            Then("검색 정보가 수정되어야 한다.") {
                memberSearchRepository.count() shouldBe 1

                val professorEntity = professorRepository.findByIdOrNull(modifiedProfessorDto.id)!!
                val memberSearchEntity = professorEntity.memberSearch
                memberSearchEntity shouldNotBe null

                memberSearchEntity?.content shouldBe """
                        modifiedName
                        역대 교수
                        modifiedAcademicRank
                        labName2
                        ${date.plusDays(1)}
                        ${date.plusDays(1)}
                        modifiedOffice
                        modifiedPhone
                        modifiedFax
                        modifiedEmail
                        modifiedWebsite
                        education1
                        modifiedEducation2
                        modifiedEducation3
                        researchArea1
                        modifiedResearchArea2
                        modifiedResearchArea3
                        career1
                        modifiedCareer2
                        modifiedCareer3
                        
                """.trimIndent()
            }
        }
    }
})
