package com.wafflestudio.csereal.core.reseach.service

import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.member.database.ProfessorEntity
import com.wafflestudio.csereal.core.member.database.ProfessorRepository
import com.wafflestudio.csereal.core.member.database.ProfessorStatus
import com.wafflestudio.csereal.core.research.database.*
import com.wafflestudio.csereal.core.research.dto.LabDto
import com.wafflestudio.csereal.core.research.dto.LabProfessorResponse
import com.wafflestudio.csereal.core.research.dto.LabUpdateRequest
import com.wafflestudio.csereal.core.research.dto.ResearchDto
import com.wafflestudio.csereal.core.research.service.ResearchService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ResearchServiceTest(
    private val researchService: ResearchService,
    private val professorRepository: ProfessorRepository,
    private val labRepository: LabRepository,
    private val researchRepository: ResearchRepository,
    private val researchSearchRepository: ResearchSearchRepository
) : BehaviorSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    beforeSpec {
    }

    afterSpec {
        professorRepository.deleteAll()
        researchRepository.deleteAll()
        labRepository.deleteAll()
        researchSearchRepository.deleteAll()
    }

    // Research
    Given("간단한 Research를 생성하려고 할 때") {
        val researchDto = ResearchDto(
            id = -1,
            language = "ko",
            name = "name",
            postType = ResearchPostType.CENTERS,
            description = "description",
            createdAt = null,
            modifiedAt = null,
            labs = null,
            imageURL = null,
            attachments = null
        )

        When("Research를 생성한다면") {
            val createdResearchDto = researchService.createResearchDetail(
                researchDto,
                null,
                null
            )

            Then("Research가 생성되어야 한다") {
                val research = researchRepository.findByIdOrNull(createdResearchDto.id)
                research shouldNotBe null
                researchRepository.count() shouldBe 1
            }

            Then("생성된 Research의 내용이 Dto와 동일해야 한다.") {
                val research = researchRepository.findByIdOrNull(createdResearchDto.id)!!
                research.name shouldBe researchDto.name
                research.postType shouldBe researchDto.postType
                research.description shouldBe researchDto.description
            }

            Then("검색 엔티티가 생성되어야 한다.") {
                val research = researchRepository.findByIdOrNull(createdResearchDto.id)!!
                val researchSearch = research.researchSearch
                researchSearch shouldNotBe null
                researchSearch!!.language shouldBe LanguageType.KO

                researchSearch!!.content shouldBe
                    """
                            name
                            연구 센터
                            description
                            
                    """.trimIndent()
            }
        }
    }

    Given("간단한 Research를 수정하려고 할 때") {
        val researchDto = ResearchDto(
            id = -1,
            language = "ko",
            name = "name",
            postType = ResearchPostType.CENTERS,
            description = "description",
            createdAt = null,
            modifiedAt = null,
            labs = null,
            imageURL = null,
            attachments = null
        )

        val createdResearchDto = researchService.createResearchDetail(
            researchDto,
            null,
            null
        )

        When("Research를 수정한다면") {
            val researchUpdateRequest = ResearchDto(
                id = createdResearchDto.id,
                language = "ko",
                name = "name2",
                postType = ResearchPostType.GROUPS,
                description = "description2",
                createdAt = null,
                modifiedAt = null,
                labs = null,
                imageURL = null,
                attachments = null
            )

            researchService.updateResearchDetail(
                createdResearchDto.id,
                researchUpdateRequest,
                null,
                null
            )

            Then("Research가 수정되어야 한다") {
                val research = researchRepository.findByIdOrNull(createdResearchDto.id)!!
                research.name shouldBe researchUpdateRequest.name
                research.postType shouldBe researchUpdateRequest.postType
                research.description shouldBe researchUpdateRequest.description
            }

            Then("검색 엔티티가 수정되어야 한다.") {
                val research = researchRepository.findByIdOrNull(createdResearchDto.id)!!
                val researchSearch = research.researchSearch
                researchSearch shouldNotBe null

                researchSearch!!.content shouldBe
                    """
                            name2
                            연구 그룹
                            description2
                            
                    """.trimIndent()
            }
        }
    }

    // Lab
    Given("pdf 없는 Lab을 생성하려고 할 때") {
        // Save professors
        val professor1 = professorRepository.save(
            ProfessorEntity(
                language = LanguageType.KO,
                name = "professor1",
                status = ProfessorStatus.ACTIVE,
                academicRank = "professor",
                email = null,
                fax = null,
                office = null,
                phone = null,
                website = null,
                startDate = null,
                endDate = null
            )
        )
        val professor2 = professorRepository.save(
            ProfessorEntity(
                language = LanguageType.KO,
                name = "professor2",
                status = ProfessorStatus.ACTIVE,
                academicRank = "professor",
                email = null,
                fax = null,
                office = null,
                phone = null,
                website = null,
                startDate = null,
                endDate = null
            )
        )

        // Save research
        val research = researchRepository.save(
            ResearchEntity(
                language = LanguageType.KO,
                name = "research",
                postType = ResearchPostType.GROUPS,
                description = null
            )
        )

        val labDto = LabDto(
            id = -1,
            language = "ko",
            name = "name",
            professors = listOf(
                LabProfessorResponse(professor1.id, professor1.name),
                LabProfessorResponse(professor2.id, professor2.name)
            ),
            acronym = "acronym",
            description = "description",
            group = "research",
            pdf = null,
            location = "location",
            tel = "tel",
            websiteURL = "websiteURL",
            youtube = "youtube"
        )

        When("Lab을 생성한다면") {
            val createdLabDto = researchService.createLab(labDto, null)

            Then("Lab이 생성되어야 한다") {
                val lab = labRepository.findByIdOrNull(createdLabDto.id)
                lab shouldNotBe null
                labRepository.count() shouldBe 1
            }

            Then("생성된 Lab의 내용이 Dto와 동일해야 한다.") {
                val lab = labRepository.findByIdOrNull(createdLabDto.id)!!
                lab.name shouldBe labDto.name
                lab.acronym shouldBe labDto.acronym
                lab.description shouldBe labDto.description
                lab.location shouldBe labDto.location
                lab.tel shouldBe labDto.tel
                lab.websiteURL shouldBe labDto.websiteURL
                lab.youtube shouldBe labDto.youtube
                lab.research shouldBe research
                lab.professors shouldBe mutableSetOf(professor1, professor2)
            }

            Then("검색 엔티티가 생성되어야 한다.") {
                val lab = labRepository.findByIdOrNull(createdLabDto.id)!!
                val researchSearch = lab.researchSearch
                researchSearch shouldNotBe null
                researchSearch!!.language shouldBe LanguageType.KO

                researchSearch!!.content shouldBe
                    """
                            name
                            professor1
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
    }

    Given("간단한 Lab을 수정할 경우") {
        // Save professors
        val professor1 = professorRepository.save(
            ProfessorEntity(
                language = LanguageType.KO,
                name = "professor1",
                status = ProfessorStatus.ACTIVE,
                academicRank = "professor",
                email = null,
                fax = null,
                office = null,
                phone = null,
                website = null,
                startDate = null,
                endDate = null
            )
        )
        val professor2 = professorRepository.save(
            ProfessorEntity(
                language = LanguageType.KO,
                name = "professor2",
                status = ProfessorStatus.ACTIVE,
                academicRank = "professor",
                email = null,
                fax = null,
                office = null,
                phone = null,
                website = null,
                startDate = null,
                endDate = null
            )
        )

        // Save research
        val research = researchRepository.save(
            ResearchEntity(
                language = LanguageType.KO,
                name = "research",
                postType = ResearchPostType.GROUPS,
                description = null
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
            description = "description",
            group = "research",
            pdf = null,
            location = "location",
            tel = "tel",
            websiteURL = "websiteURL",
            youtube = "youtube"
        )

        val createdLabDto = researchService.createLab(labDto, null)
        val createdLab = labRepository.findByIdOrNull(createdLabDto.id)!!

        When("pdf를 제외하고 Lab을 수정한다면") {
            val professor3 = professorRepository.save(
                ProfessorEntity(
                    language = LanguageType.KO,
                    name = "professor3",
                    status = ProfessorStatus.ACTIVE,
                    academicRank = "professor",
                    email = null,
                    fax = null,
                    office = null,
                    phone = null,
                    website = null,
                    startDate = null,
                    endDate = null
                )
            )

            val labUpdateRequest = LabUpdateRequest(
                name = "name2",
                professorIds = listOf(professor1.id, professor3.id),
                acronym = "acronym2",
                description = "description2",
                location = "location2",
                tel = "tel2",
                websiteURL = "websiteURL2",
                youtube = "youtube2",
                pdfModified = false
            )

            researchService.updateLab(createdLab.id, labUpdateRequest, null)

            Then("Lab이 수정되어야 한다.") {
                val lab = labRepository.findByIdOrNull(createdLab.id)!!
                lab.name shouldBe labUpdateRequest.name
                lab.acronym shouldBe labUpdateRequest.acronym
                lab.description shouldBe labUpdateRequest.description
                lab.location shouldBe labUpdateRequest.location
                lab.tel shouldBe labUpdateRequest.tel
                lab.websiteURL shouldBe labUpdateRequest.websiteURL
                lab.youtube shouldBe labUpdateRequest.youtube
                lab.research shouldBe research
                lab.professors shouldBe mutableSetOf(professor1, professor3)
            }

            Then("검색 엔티티가 수정되어야 한다.") {
                val lab = labRepository.findByIdOrNull(createdLab.id)!!
                val researchSearch = lab.researchSearch
                researchSearch shouldNotBe null

                researchSearch!!.content shouldBe
                    """
                            name2
                            professor1
                            professor3
                            location2
                            tel2
                            acronym2
                            youtube2
                            research
                            description2
                            websiteURL2
                            
                    """.trimIndent()
            }
        }
    }
})
