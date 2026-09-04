package com.wafflestudio.csereal.core.reseach.service

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.database.ProfessorRepository
import com.wafflestudio.csereal.core.research.api.req.CreateResearchLanguageReqBody
import com.wafflestudio.csereal.core.research.api.req.ModifyResearchLanguageReqBody
import com.wafflestudio.csereal.core.research.api.req.ResearchContentReqBody
import com.wafflestudio.csereal.core.research.database.LabRepository
import com.wafflestudio.csereal.core.research.database.ResearchRepository
import com.wafflestudio.csereal.core.research.database.ResearchSearchRepository
import com.wafflestudio.csereal.core.research.database.ResearchTranslationRepository
import com.wafflestudio.csereal.core.research.service.ResearchService
import com.wafflestudio.csereal.core.research.type.ResearchType
import com.wafflestudio.csereal.global.config.MySQLTestContainerConfig
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import jakarta.transaction.Transactional
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(MySQLTestContainerConfig::class)
class ResearchServiceTest(
    private val researchService: ResearchService,
    private val researchRepository: ResearchRepository,
    private val researchTranslationRepository: ResearchTranslationRepository,
    private val researchSearchRepository: ResearchSearchRepository,
    private val professorRepository: ProfessorRepository,
    private val labRepository: LabRepository
) : BehaviorSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    afterSpec {
        professorRepository.deleteAll()
        researchRepository.deleteAll()
        labRepository.deleteAll()
        researchSearchRepository.deleteAll()
    }

    fun createReq(type: ResearchType, websiteURL: String? = null) = CreateResearchLanguageReqBody(
        type = type,
        websiteURL = websiteURL,
        ko = ResearchContentReqBody("한국어 이름", "한국어 설명입니다."),
        en = ResearchContentReqBody("English Name", "This is an English description.")
    )

    Given("연구센터를 만들려고 할 때") {
        When("생성하면") {
            val created = researchService.createResearchLanguage(
                createReq(ResearchType.CENTERS, "https://www.center.com"),
                null
            )

            Then("연구 하나에 번역본 두 개가 생긴다") {
                researchRepository.count() shouldBe 1
                researchTranslationRepository.count() shouldBe 2
            }

            Then("종류와 웹사이트는 응답 최상위에 한 벌만 있다") {
                val research = researchRepository.findByIdOrNull(created.id)!!
                research.postType shouldBe ResearchType.CENTERS
                created.type shouldBe ResearchType.CENTERS
                created.websiteURL shouldBe "https://www.center.com"
            }

            Then("이름과 설명은 언어별로 다르다") {
                created.ko!!.name shouldBe "한국어 이름"
                created.en!!.name shouldBe "English Name"
            }
        }
    }

    Given("연구그룹이 있을 때") {
        val created = researchService.createResearchLanguage(createReq(ResearchType.GROUPS), null)

        When("수정하면") {
            val modified = researchService.updateResearchLanguage(
                // 어느 쪽 언어의 id 로 불러도 같은 연구를 가리킨다.
                created.id,
                ModifyResearchLanguageReqBody(
                    websiteURL = null,
                    removeImage = false,
                    ko = ResearchContentReqBody("바뀐 이름", "바뀐 설명"),
                    en = ResearchContentReqBody("Changed Name", "Changed description")
                ),
                null
            )

            Then("두 언어가 함께 바뀐다") {
                researchRepository.count() shouldBe 1
                val research = researchRepository.findByIdOrNull(modified.id)!!
                research.translationOf(LanguageType.KO)!!.name shouldBe "바뀐 이름"
                research.translationOf(LanguageType.EN)!!.name shouldBe "Changed Name"
            }
        }

        When("삭제하면") {
            researchService.deleteResearchLanguage(created.id)

            Then("번역본까지 함께 지워진다") {
                researchRepository.count() shouldBe 0
                researchTranslationRepository.count() shouldBe 0
            }
        }
    }
})
