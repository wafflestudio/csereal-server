package com.wafflestudio.csereal.core.conference.service

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.conference.database.ConferenceEntity
import com.wafflestudio.csereal.core.conference.database.ConferencePageEntity
import com.wafflestudio.csereal.core.conference.database.ConferencePageRepository
import com.wafflestudio.csereal.core.conference.database.ConferenceRepository
import com.wafflestudio.csereal.core.conference.dto.ConferenceDto
import com.wafflestudio.csereal.core.conference.dto.ConferenceModifyRequest
import com.wafflestudio.csereal.core.user.database.UserRepository
import com.wafflestudio.csereal.core.user.service.UserService
import com.wafflestudio.csereal.global.config.MySQLTestContainerConfig
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@Import(MySQLTestContainerConfig::class)
class ConferenceServiceTest(
    private val conferenceService: ConferenceService,
    private val conferencePageRepository: ConferencePageRepository,
    private val conferenceRepository: ConferenceRepository,
    private val userRepository: UserRepository,
    private val userService: UserService
) : BehaviorSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    beforeSpec {
        conferencePageRepository.save(
            ConferencePageEntity(
                author = userService.getLoginUser()
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
        var conferencePage = conferencePageRepository.findAll().first()

        val conferences = conferenceRepository.saveAll(
            listOf(
                ConferenceEntity(
                    language = LanguageType.KO,
                    name = "name1",
                    abbreviation = "abbreviation1",
                    conferencePage = conferencePage
                ),
                ConferenceEntity(
                    language = LanguageType.KO,
                    name = "name2",
                    abbreviation = "abbreviation2",
                    conferencePage = conferencePage
                ),
                ConferenceEntity(
                    language = LanguageType.KO,
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
                name = "modifiedName",
                abbreviation = "modifiedAbbreviation"
            )
            val newConference = ConferenceDto(
                language = "ko",
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
                val newConferences = newConferencePage.conferences.sortedBy { it.name }

                newConferences.size shouldBe 3
                newConferences.first().apply {
                    name shouldBe modifiedConference.name
                    abbreviation shouldBe modifiedConference.abbreviation
                    researchSearch?.content shouldBe """
                        modifiedName
                        modifiedAbbreviation
                        
                    """.trimIndent()
                }
                newConferences[1].apply {
                    name shouldBe conferences.last().name
                    abbreviation shouldBe conferences.last().abbreviation
                }
                newConferences.last().apply {
                    name shouldBe newConference.name
                    abbreviation shouldBe newConference.abbreviation
                    researchSearch?.content shouldBe """
                        newName
                        newAbbreviation
                        
                    """.trimIndent()
                }
            }
        }
    }
})
