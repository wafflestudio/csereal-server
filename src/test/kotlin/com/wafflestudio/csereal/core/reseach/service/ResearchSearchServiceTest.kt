package com.wafflestudio.csereal.core.reseach.service

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.api.req.CreateProfessorReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyProfessorReqBody
import com.wafflestudio.csereal.core.member.database.ProfessorRepository
import com.wafflestudio.csereal.core.member.database.ProfessorStatus
import com.wafflestudio.csereal.core.member.service.ProfessorService
import com.wafflestudio.csereal.core.research.database.*
import com.wafflestudio.csereal.core.research.dto.LabDto
import com.wafflestudio.csereal.core.research.dto.LabProfessorResponse
import com.wafflestudio.csereal.core.research.service.ResearchSearchService
import com.wafflestudio.csereal.core.research.service.ResearchService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@Transactional
class ResearchSearchServiceTest(
    private val researchSearchService: ResearchSearchService,
    private val professorRepository: ProfessorRepository,
    private val professorService: ProfessorService,
    private val labRepository: LabRepository,
    private val researchRepository: ResearchRepository,
    private val researchSearchRepository: ResearchSearchRepository,
    private val researchService: ResearchService
) : BehaviorSpec() {
    init {
        extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

        beforeSpec {
        }

        afterSpec {
            professorRepository.deleteAll()
            labRepository.deleteAll()
            researchSearchRepository.deleteAll()
        }

        // Event Listener Test
        Given("기존 lab이 존재할 때") {
            // Save professors
            val professor1Dto = professorService.createProfessor(
                CreateProfessorReqBody(
                    language = "ko",
                    name = "professor1",
                    email = null,
                    status = ProfessorStatus.ACTIVE,
                    academicRank = "professor",
                    labId = null,
                    startDate = null,
                    endDate = null,
                    office = null,
                    phone = null,
                    fax = null,
                    website = null,
                    educations = emptyList(),
                    researchAreas = emptyList(),
                    careers = emptyList()
                ),
                mainImage = null
            )
            val professor2Dto = professorService.createProfessor(
                CreateProfessorReqBody(
                    language = "ko",
                    name = "professor2",
                    email = null,
                    status = ProfessorStatus.ACTIVE,
                    academicRank = "professor",
                    labId = null,
                    startDate = null,
                    endDate = null,
                    office = null,
                    phone = null,
                    fax = null,
                    website = null,
                    educations = emptyList(),
                    researchAreas = emptyList(),
                    careers = emptyList()
                ),
                mainImage = null
            )

            val professor1 = professorRepository.findByIdOrNull(professor1Dto.id)!!
            val professor2 = professorRepository.findByIdOrNull(professor2Dto.id)!!

            // Save research
            val research = researchRepository.save(
                ResearchEntity(
                    language = LanguageType.KO,
                    name = "research",
                    postType = ResearchPostType.GROUPS,
                    description = null,
                    websiteURL = null
                )
            )

            // Save lab
            val labDto = LabDto(
                id = -1,
                language = "ko",
                name = "name",
                professors = listOf(
                    LabProfessorResponse(professor1.id, professor1.name),
                    LabProfessorResponse(professor2.id, professor2.name)
                ),
                acronym = "acronym",
                description = "<p>description</p>",
                group = "research",
                pdf = null,
                location = "location",
                tel = "tel",
                websiteURL = "websiteURL",
                youtube = "youtube"
            )

            val emptyLabDto = LabDto(
                id = -1,
                language = "ko",
                name = "nameE",
                professors = listOf(),
                acronym = "acronymE",
                description = "<h1>descriptionE</h1>",
                group = "research",
                pdf = null,
                location = "locationE",
                tel = "telE",
                websiteURL = "websiteURLE",
                youtube = "youtubeE"
            )

            val createdLabDto = researchService.createLab(labDto, null)
            val createdEmptyLabDto = researchService.createLab(emptyLabDto, null)

            When("professor가 제거된다면") {
                professorService.deleteProfessor(professor1.id)

                Then("검색 엔티티의 내용이 변경된다") {
                    val lab = labRepository.findByIdOrNull(createdLabDto.id)!!
                    val search = lab.researchSearch

                    search shouldNotBe null
                    search!!.content shouldBe
                        """
                        name
                        professor2
                        location
                        tel
                        acronym
                        youtube
                        research
                        description
                        websiteURL
                        
                        """.trimIndent()
                }
            }

            When("professor가 추가된다면") {
                val process3CreatedDto = professorService.createProfessor(
                    CreateProfessorReqBody(
                        language = "ko",
                        name = "newProfessor",
                        email = "email",
                        status = ProfessorStatus.ACTIVE,
                        academicRank = "academicRank",
                        labId = createdLabDto.id,
                        startDate = LocalDate.now(),
                        endDate = LocalDate.now(),
                        office = "office",
                        phone = "phone",
                        fax = "fax",
                        website = "website",
                        educations = listOf("education1", "education2"),
                        researchAreas = listOf("researchArea1", "researchArea2"),
                        careers = listOf("career1", "career2")
                    ),
                    mainImage = null
                )

                Then("검색 엔티티의 내용이 변경된다") {
                    val lab = labRepository.findByIdOrNull(createdLabDto.id)!!
                    val search = lab.researchSearch

                    search shouldNotBe null
                    search!!.content shouldBe
                        """
                        name
                        professor2
                        newProfessor
                        location
                        tel
                        acronym
                        youtube
                        research
                        description
                        websiteURL
                        
                        """.trimIndent()
                }
            }

            When("professor가 수정된다면") {
                professorService.updateProfessor(
                    professor2.id,
                    professor2.run {
                        ModifyProfessorReqBody(
                            language = "ko",
                            name = "updateProfessor",
                            status = status,
                            academicRank = academicRank,
                            labId = createdEmptyLabDto.id,
                            startDate = startDate,
                            endDate = endDate,
                            office = office,
                            phone = phone,
                            fax = fax,
                            email = email,
                            website = website,
                            educations = educations.map { it.name },
                            researchAreas = researchAreas.map { it.name },
                            careers = careers.map { it.name },
                            removeImage = false
                        )
                    },
                    mainImage = null
                )

                Then("예전 검색 데이터에서 빠져야 한다.") {
                    val lab = labRepository.findByIdOrNull(createdLabDto.id)!!
                    val search = lab.researchSearch

                    search shouldNotBe null
                    search!!.content shouldBe
                        """
                        name
                        newProfessor
                        location
                        tel
                        acronym
                        youtube
                        research
                        description
                        websiteURL
                        
                        """.trimIndent()
                }

                Then("새로운 검색 데이터에 포함되어야 한다.") {
                    val lab = labRepository.findByIdOrNull(createdEmptyLabDto.id)!!
                    val search = lab.researchSearch

                    search shouldNotBe null
                    search!!.content shouldBe
                        """
                        nameE
                        updateProfessor
                        locationE
                        telE
                        acronymE
                        youtubeE
                        research
                        descriptionE
                        websiteURLE
                        
                        """.trimIndent()
                }
            }
        }
    }
}
