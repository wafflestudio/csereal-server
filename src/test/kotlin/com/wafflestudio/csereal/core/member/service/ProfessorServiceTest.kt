package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.core.member.database.MemberSearchRepository
import com.wafflestudio.csereal.core.member.database.ProfessorRepository
import com.wafflestudio.csereal.core.member.database.ProfessorStatus
import com.wafflestudio.csereal.core.member.dto.ProfessorDto
import com.wafflestudio.csereal.core.research.database.*
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.transaction.Transactional
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate

@SpringBootTest
@Transactional
class ProfessorServiceTest(
    private val professorService: ProfessorService,
    private val professorRepository: ProfessorRepository,
    private val labRepository: LabRepository,
    private val memberSearchRepository: MemberSearchRepository,
    private val researchRepository: ResearchRepository
) : BehaviorSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    afterContainer {
        professorRepository.deleteAll()
        researchRepository.deleteAll()
    }

    Given("이미지 없는 교수를 생성하려고 할 때") {
        val date = LocalDate.now()

        val researchEntity = ResearchEntity(
            name = "researchName",
            description = null,
            postType = ResearchPostType.LABS
        )
        var labEntity = LabEntity(
            name = "labName",
            location = null,
            tel = null,
            acronym = null,
            youtube = null,
            description = null,
            websiteURL = null,
            research = researchEntity
        )
        researchEntity.labs.add(labEntity)
        researchRepository.save(researchEntity)
        labEntity = labRepository.save(labEntity)

        val professorDto = ProfessorDto(
            language = "ko",
            name = "name",
            email = "email",
            status = ProfessorStatus.ACTIVE,
            academicRank = "academicRank",
            labId = labEntity.id,
            labName = null,
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
            val createdProfessorDto = professorService.createProfessor(professorDto, null)

            Then("교수가 생성되어야 한다") {
                professorRepository.count() shouldBe 1
                professorRepository.findByIdOrNull(createdProfessorDto.id) shouldNotBe null
            }

            Then("교수의 정보가 일치해야 한다") {
                val professorEntity = professorRepository.findByIdOrNull(createdProfessorDto.id)!!

                professorEntity.name shouldBe professorDto.name
                professorEntity.email shouldBe professorDto.email
                professorEntity.status shouldBe professorDto.status
                professorEntity.academicRank shouldBe professorDto.academicRank
                professorEntity.lab shouldBe labEntity
                professorEntity.startDate shouldBe professorDto.startDate
                professorEntity.endDate shouldBe professorDto.endDate
                professorEntity.office shouldBe professorDto.office
                professorEntity.phone shouldBe professorDto.phone
                professorEntity.fax shouldBe professorDto.fax
                professorEntity.website shouldBe professorDto.website
                professorEntity.educations.map { it.name } shouldBe professorDto.educations
                professorEntity.researchAreas.map { it.name } shouldBe professorDto.researchAreas
                professorEntity.careers.map { it.name } shouldBe professorDto.careers
            }

            Then("교수의 검색 정보가 생성되어야 한다") {
                memberSearchRepository.count() shouldBe 1
                val memberSearchEntity = memberSearchRepository.findAll()[0]

                memberSearchEntity.professor?.id shouldBe createdProfessorDto.id

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
        val researchEntity = ResearchEntity(
            name = "researchName",
            description = null,
            postType = ResearchPostType.LABS
        )
        val labEntity1 = LabEntity(
            name = "labName1",
            location = null,
            tel = null,
            acronym = null,
            youtube = null,
            description = null,
            websiteURL = null,
            research = researchEntity
        )
        val labEntity2 = LabEntity(
            name = "labName2",
            location = null,
            tel = null,
            acronym = null,
            youtube = null,
            description = null,
            websiteURL = null,
            research = researchEntity
        )
        researchEntity.labs.addAll(listOf(labEntity1, labEntity2))
        researchRepository.save(researchEntity)

        val createdProfessorDto = professorService.createProfessor(
            ProfessorDto(
                language = "ko",
                name = "name",
                email = "email",
                status = ProfessorStatus.ACTIVE,
                academicRank = "academicRank",
                labId = labEntity1.id,
                labName = null,
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
            val toModifyProfessorDto = createdProfessorDto.copy(
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
                careers = listOf("career1", "modifiedCareer2", "modifiedCareer3")
            )

            val modifiedProfessorDto = professorService.updateProfessor(
                toModifyProfessorDto.id!!,
                toModifyProfessorDto,
                null
            )

            Then("교수 정보가 수정되어야 한다.") {
                professorRepository.count() shouldBe 1
                val professorEntity = professorRepository.findByIdOrNull(modifiedProfessorDto.id)
                professorEntity shouldNotBe null

                professorEntity!!.name shouldBe toModifyProfessorDto.name
                professorEntity.email shouldBe toModifyProfessorDto.email
                professorEntity.status shouldBe toModifyProfessorDto.status
                professorEntity.academicRank shouldBe toModifyProfessorDto.academicRank
                professorEntity.lab shouldBe labEntity2
                professorEntity.startDate shouldBe toModifyProfessorDto.startDate
                professorEntity.endDate shouldBe toModifyProfessorDto.endDate
                professorEntity.office shouldBe toModifyProfessorDto.office
                professorEntity.phone shouldBe toModifyProfessorDto.phone
                professorEntity.fax shouldBe toModifyProfessorDto.fax
                professorEntity.website shouldBe toModifyProfessorDto.website
                professorEntity.educations.map { it.name } shouldBe toModifyProfessorDto.educations
                professorEntity.researchAreas.map { it.name } shouldBe toModifyProfessorDto.researchAreas
                professorEntity.careers.map { it.name } shouldBe toModifyProfessorDto.careers
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
