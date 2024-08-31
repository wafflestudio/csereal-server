package com.wafflestudio.csereal.core.reseach.service

import com.wafflestudio.csereal.core.member.database.ProfessorRepository
import com.wafflestudio.csereal.core.research.api.req.*
import com.wafflestudio.csereal.core.research.database.LabRepository
import com.wafflestudio.csereal.core.research.database.ResearchLanguageRepository
import com.wafflestudio.csereal.core.research.database.ResearchRepository
import com.wafflestudio.csereal.core.research.database.ResearchSearchRepository
import com.wafflestudio.csereal.core.research.dto.ResearchLanguageDto
import com.wafflestudio.csereal.core.research.dto.ResearchSealedDto
import com.wafflestudio.csereal.core.research.service.ResearchService
import com.wafflestudio.csereal.core.research.type.ResearchRelatedType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ResearchServiceTest(
    private val researchService: ResearchService,
    private val researchLanguageRepository: ResearchLanguageRepository,
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
        researchLanguageRepository.deleteAll()
        researchRepository.deleteAll()
        labRepository.deleteAll()
        researchSearchRepository.deleteAll()
    }

    // TODO: Add edge test cases
    // TODO: Add search index test cases
    Given("Create Research Center Request Body") {
        val koCreateResearchCenterReqBody = CreateResearchCenterReqBody(
            name = "한국어 연구소",
            description = "한국어 연구소입니다.",
            mainImageUrl = null,
            websiteURL = "https://www.koreanlab.com",
        )

        val enCreateResearchCenterReqBody = CreateResearchCenterReqBody(
            name = "English Research Center",
            description = "This is English Research Center.",
            mainImageUrl = null,
            websiteURL = "https://www.englishlab.com",
        )

        val createResearchCenterReqBody = CreateResearchLanguageReqBody(
            ko = koCreateResearchCenterReqBody,
            en = enCreateResearchCenterReqBody
        )

        When("Create Research Center") {
            val researchCenter = researchService.createResearchLanguage(createResearchCenterReqBody, null)

            Then("Research Center should be created") {
                val pair = researchLanguageRepository.findAll()
                    .also { it.size shouldBe 1 }
                pair[0].type shouldBe ResearchRelatedType.RESEARCH_CENTER

                val (koId, enId) = pair[0].koreanId to pair[0].englishId
                val koResearchCenter = researchRepository.findByIdOrNull(koId)!!
                val enResearchCenter = researchRepository.findByIdOrNull(enId)!!
                ResearchLanguageDto(
                    ko = ResearchSealedDto.of(koResearchCenter, null),
                    en = ResearchSealedDto.of(enResearchCenter, null),
                ) shouldBe researchCenter
            }
        }
    }

    Given("Create Research Group Request Body") {
        val koCreateResearchGroupReqBody = CreateResearchGroupReqBody(
            name = "한국어 연구 그룹",
            description = "한국어 연구 그룹입니다.",
            mainImageUrl = null,
        )

        val enCreateResearchGroupReqBody = CreateResearchGroupReqBody(
            name = "English Research Group",
            description = "This is English Research Group.",
            mainImageUrl = null,
        )

        val createResearchGroupReqBody = CreateResearchLanguageReqBody(
            ko = koCreateResearchGroupReqBody,
            en = enCreateResearchGroupReqBody
        )

        When("Create Research Group") {
            val researchGroup = researchService.createResearchLanguage(createResearchGroupReqBody, null)

            Then("Research Group should be created") {
                val pair = researchLanguageRepository.findAll()
                    .also { it.size shouldBe 1 }
                pair[0].type shouldBe ResearchRelatedType.RESEARCH_GROUP

                val (koId, enId) = pair[0].koreanId to pair[0].englishId
                val koResearchGroup = researchRepository.findByIdOrNull(koId)!!
                val enResearchGroup = researchRepository.findByIdOrNull(enId)!!
                ResearchLanguageDto(
                    ko = ResearchSealedDto.of(koResearchGroup, null),
                    en = ResearchSealedDto.of(enResearchGroup, null),
                ) shouldBe researchGroup
            }
        }
    }

    Given("Research Center Exists") {
        val koCreateResearchCenterReqBody = CreateResearchCenterReqBody(
            name = "한국어 연구소",
            description = "한국어 연구소입니다.",
            mainImageUrl = null,
            websiteURL = "https://www.koreanlab.com",
        )

        val enCreateResearchCenterReqBody = CreateResearchCenterReqBody(
            name = "English Research Center",
            description = "This is English Research Center.",
            mainImageUrl = null,
            websiteURL = "https://www.englishlab.com",
        )

        val createResearchCenterReqBody = CreateResearchLanguageReqBody(
            ko = koCreateResearchCenterReqBody,
            en = enCreateResearchCenterReqBody
        )

        val researchCenter = researchService.createResearchLanguage(createResearchCenterReqBody, null)

        When("Update Research Center") {
            val koUpdateResearchCenterReqBody = ModifyResearchCenterReqBody(
                name = "한국어 연구소 수정",
                description = "한국어 연구소입니다. 수정",
                websiteURL = "https://www.koreanlabbbb.com",
                removeImage = false,
            )

            val enUpdateResearchCenterReqBody = ModifyResearchCenterReqBody(
                name = "English Research Center Update",
                description = "This is English Research Center. Update",
                websiteURL = "https://www.englishlabbbb.com",
                removeImage = false,
            )

            val updateResearchCenterReqBody = ModifyResearchLanguageReqBody(
                ko = koUpdateResearchCenterReqBody,
                en = enUpdateResearchCenterReqBody
            )

            val modifiedResearchCenter = researchService.updateResearchLanguage(
                researchCenter.ko.id,
                researchCenter.en.id,
                updateResearchCenterReqBody,
                null
            )

            Then("Research Center should be updated") {
                val pair = researchLanguageRepository.findAll()
                    .also { it.size shouldBe 1 }
                pair[0].type shouldBe ResearchRelatedType.RESEARCH_CENTER
                val (koId, enId) = pair[0].koreanId to pair[0].englishId
                koId shouldBe researchCenter.ko.id
                enId shouldBe researchCenter.en.id

                val koResearchCenter = researchRepository.findByIdOrNull(koId)!!
                val enResearchCenter = researchRepository.findByIdOrNull(enId)!!
                ResearchLanguageDto(
                    ko = ResearchSealedDto.of(koResearchCenter, null),
                    en = ResearchSealedDto.of(enResearchCenter, null),
                ) shouldBe modifiedResearchCenter
            }
        }

        When("Delete Research Center") {
            researchService.deleteResearchLanguage(researchCenter.ko.id, researchCenter.en.id)

            Then("Research Center should be deleted") {
                researchLanguageRepository.findAll() shouldBe emptyList()
                researchRepository.findAll() shouldBe emptyList()
            }
        }
    }

    Given("Research Group Exists") {
        val koCreateResearchGroupReqBody = CreateResearchGroupReqBody(
            name = "한국어 연구 그룹",
            description = "한국어 연구 그룹입니다.",
            mainImageUrl = null,
        )

        val enCreateResearchGroupReqBody = CreateResearchGroupReqBody(
            name = "English Research Group",
            description = "This is English Research Group.",
            mainImageUrl = null,
        )

        val createResearchGroupReqBody = CreateResearchLanguageReqBody(
            ko = koCreateResearchGroupReqBody,
            en = enCreateResearchGroupReqBody
        )

        val researchGroup = researchService.createResearchLanguage(createResearchGroupReqBody, null)

        When("Update Research Group") {
            val koUpdateResearchGroupReqBody = ModifyResearchGroupReqBody(
                name = "한국어 연구 그룹 수정",
                description = "한국어 연구 그룹입니다. 수정",
                removeImage = false,
            )

            val enUpdateResearchGroupReqBody = ModifyResearchGroupReqBody(
                name = "English Research Group Update",
                description = "This is English Research Group. Update",
                removeImage = false,
            )

            val updateResearchGroupReqBody = ModifyResearchLanguageReqBody(
                ko = koUpdateResearchGroupReqBody,
                en = enUpdateResearchGroupReqBody
            )

            val modifiedResearchGroup = researchService.updateResearchLanguage(
                researchGroup.ko.id,
                researchGroup.en.id,
                updateResearchGroupReqBody,
                null
            )

            Then("Research Group should be updated") {
                val pair = researchLanguageRepository.findAll()
                    .also { it.size shouldBe 1 }
                pair[0].type shouldBe ResearchRelatedType.RESEARCH_GROUP
                val (koId, enId) = pair[0].koreanId to pair[0].englishId
                koId shouldBe researchGroup.ko.id
                enId shouldBe researchGroup.en.id

                val koResearchGroup = researchRepository.findByIdOrNull(koId)!!
                val enResearchGroup = researchRepository.findByIdOrNull(enId)!!
                ResearchLanguageDto(
                    ko = ResearchSealedDto.of(koResearchGroup, null),
                    en = ResearchSealedDto.of(enResearchGroup, null),
                ) shouldBe modifiedResearchGroup
            }
        }

        When("Delete Research Group") {
            researchService.deleteResearchLanguage(researchGroup.ko.id, researchGroup.en.id)

            Then("Research Group should be deleted") {
                researchLanguageRepository.findAll() shouldBe emptyList()
                researchRepository.findAll() shouldBe emptyList()
            }
        }
    }
})
