package com.wafflestudio.csereal.core.admissions.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.admissions.api.req.AdmissionReqBody
import com.wafflestudio.csereal.core.admissions.database.AdmissionsEntity
import com.wafflestudio.csereal.core.admissions.database.AdmissionsRepository
import com.wafflestudio.csereal.core.admissions.type.AdmissionsMainType
import com.wafflestudio.csereal.core.admissions.type.AdmissionsPostType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Profile("test")
@Transactional
class AdmissionsServiceTest(
    private val admissionsService: AdmissionsService,
    private val admissionsRepository: AdmissionsRepository
) : BehaviorSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    afterContainer {
        admissionsRepository.deleteAll()
    }

    Given("AdmissionReqBody, AdmissionMainType, AdmissionPostType이 주어졌을 때") {
        val req = AdmissionReqBody(
            name = "name",
            language = "ko",
            description = "<p>description</p>"
        )
        val mainType = AdmissionsMainType.UNDERGRADUATE
        val postType = AdmissionsPostType.REGULAR_ADMISSION

        When("createAdmission이 호출되면") {
            val result = admissionsService.createAdmission(req, mainType, postType)

            Then("주어진 정보와 일치하는 AdmissionDto가 반환된다.") {
                result.name shouldBe req.name
                result.mainType shouldBe mainType.toJsonValue()
                result.postType shouldBe postType.toJsonValue()
                result.language shouldBe req.language
                result.description shouldBe req.description
            }

            Then("주어진 정보와 일치하는 AdmissionEnitity가 생성된다.") {
                val entity = admissionsRepository.findByIdOrNull(result.id)
                entity shouldNotBe null
                entity!!.name shouldBe req.name
                entity.mainType shouldBe mainType
                entity.postType shouldBe postType
                entity.language shouldBe LanguageType.makeStringToLanguageType(req.language)
                entity.description shouldBe req.description
            }

            Then("검색 정보가 잘 생성되어야 한다.") {
                val entity = admissionsRepository.findByIdOrNull(result.id)!!
                entity.searchContent shouldBe """
                    ${req.name}
                    ${mainType.getLanguageValue(LanguageType.KO)}
                    ${postType.getLanguageValue(LanguageType.KO)}
                    description
                    
                """.trimIndent()
            }
        }
    }
    Given("AdmissionReqBody에 잘못된 Language가 주어졌을 때") {
        val req = AdmissionReqBody(
            name = "name",
            language = "wrong",
            description = "description"
        )
        val mainType = AdmissionsMainType.UNDERGRADUATE
        val postType = AdmissionsPostType.REGULAR_ADMISSION
        When("createAdmission이 호출되면") {
            Then("Csereal400 에러가 발생한다.") {
                shouldThrow<CserealException.Csereal400> {
                    admissionsService.createAdmission(req, mainType, postType)
                }
            }
        }
    }

    Given("Admission이 존재할 때") {
        val admission = AdmissionsEntity(
            name = "name",
            mainType = AdmissionsMainType.INTERNATIONAL,
            postType = AdmissionsPostType.EXCHANGE_VISITING,
            language = LanguageType.EN,
            description = "description",
            searchContent = "ss"
        ).let {
            admissionsRepository.save(it)
        }

        When("존재하는 readAdmission이 호출되면") {
            val mainType = AdmissionsMainType.INTERNATIONAL
            val postType = AdmissionsPostType.EXCHANGE_VISITING
            val language = LanguageType.EN
            val result = admissionsService.readAdmission(mainType, postType, language)

            Then("주어진 정보와 일치하는 AdmissionDto가 반환된다.") {
                result.let {
                    it.name shouldBe admission.name
                    it.mainType shouldBe admission.mainType.toJsonValue()
                    it.postType shouldBe admission.postType.toJsonValue()
                    it.language shouldBe LanguageType.makeLowercase(admission.language)
                    it.description shouldBe admission.description
                }
            }
        }

        When("존재하지 않는 readAdmission이 호출되면") {
            Then("Csereal404 에러가 발생한다.") {
                shouldThrow<CserealException.Csereal404> {
                    admissionsService.readAdmission(
                        AdmissionsMainType.UNDERGRADUATE,
                        AdmissionsPostType.REGULAR_ADMISSION,
                        LanguageType.KO
                    )
                }
            }
        }
    }
})
