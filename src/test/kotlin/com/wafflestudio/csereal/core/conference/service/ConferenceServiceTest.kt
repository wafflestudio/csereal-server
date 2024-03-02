package com.wafflestudio.csereal.core.conference.service

import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.conference.database.ConferenceEntity
import com.wafflestudio.csereal.core.conference.database.ConferencePageEntity
import com.wafflestudio.csereal.core.conference.database.ConferencePageRepository
import com.wafflestudio.csereal.core.conference.database.ConferenceRepository
import com.wafflestudio.csereal.core.conference.dto.ConferenceDto
import com.wafflestudio.csereal.core.conference.dto.ConferenceModifyRequest
import com.wafflestudio.csereal.core.user.database.Role
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

@SpringBootTest
@Transactional
class ConferenceServiceTest(
    private val conferenceService: ConferenceService,
    private val conferencePageRepository: ConferencePageRepository,
    private val conferenceRepository: ConferenceRepository,
    private val userRepository: UserRepository
) : BehaviorSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    beforeSpec {
        val user = userRepository.save(
            UserEntity(
                username = "admin",
                name = "admin",
                email = "email",
                studentId = "studentId",
                role = Role.ROLE_STAFF
            )
        )

        conferencePageRepository.save(
            ConferencePageEntity(
                author = user
            )
        )
    }

    afterSpec {
        conferencePageRepository.deleteAll()
        conferenceRepository.deleteAll()
        userRepository.deleteAll()
    }

    // ConferencePage
    Given("Conference를 수정하려고 할 때") {
        val userEntity = userRepository.findByUsername("admin")!!

        mockkStatic(RequestContextHolder::class)
        val mockRequestAttributes = mockk<RequestAttributes>()
        every {
            RequestContextHolder.getRequestAttributes()
        } returns mockRequestAttributes
        every {
            mockRequestAttributes.getAttribute(
                "loggedInUser",
                RequestAttributes.SCOPE_REQUEST
            )
        } returns userEntity

        var conferencePage = conferencePageRepository.findAll().first()

        val conferences = conferenceRepository.saveAll(
            listOf(
                ConferenceEntity(
                    language = LanguageType.KO,
                    code = "code1",
                    name = "name1",
                    abbreviation = "abbreviation1",
                    conferencePage = conferencePage
                ),
                ConferenceEntity(
                    language = LanguageType.KO,
                    code = "code2",
                    name = "name2",
                    abbreviation = "abbreviation2",
                    conferencePage = conferencePage
                ),
                ConferenceEntity(
                    language = LanguageType.KO,
                    code = "code3",
                    name = "name3",
                    abbreviation = "abbreviation3",
                    conferencePage = conferencePage
                )
            )
        )
        conferencePage = conferencePage.apply {
            this.conferences.addAll(conferences)
        }.let {
            conferencePageRepository.save(it)
        }

        When("Conference를 수정한다면") {
            val deleteConferenceId = conferences[1].id
            val modifiedConference = ConferenceDto(
                id = conferences.first().id,
                language = "ko",
                code = "code0",
                name = "modifiedName",
                abbreviation = "modifiedAbbreviation"
            )
            val newConference = ConferenceDto(
                language = "ko",
                code = "code9",
                name = "newName",
                abbreviation = "newAbbreviation"
            )
            val conferenceModifyRequest = ConferenceModifyRequest(
                deleteConferenceIdList = listOf(deleteConferenceId),
                modifiedConferenceList = listOf(modifiedConference),
                newConferenceList = listOf(newConference)
            )

            val conferencePage = conferenceService.modifyConferences(conferenceModifyRequest)

            Then("Conference가 수정되어야 한다.") {
                val newConferencePage = conferencePageRepository.findAll().first()
                val newConferences = newConferencePage.conferences.sortedBy { it.code }

                newConferences.size shouldBe 3
                newConferences.first().apply {
                    code shouldBe modifiedConference.code
                    name shouldBe modifiedConference.name
                    abbreviation shouldBe modifiedConference.abbreviation
                    researchSearch?.content shouldBe """
                        modifiedName
                        code0
                        modifiedAbbreviation
                        
                    """.trimIndent()
                }
                newConferences[1].apply {
                    code shouldBe conferences.last().code
                    name shouldBe conferences.last().name
                    abbreviation shouldBe conferences.last().abbreviation
                }
                newConferences.last().apply {
                    code shouldBe newConference.code
                    name shouldBe newConference.name
                    abbreviation shouldBe newConference.abbreviation
                    researchSearch?.content shouldBe """
                        newName
                        code9
                        newAbbreviation
                        
                    """.trimIndent()
                }
            }
        }
    }
})
