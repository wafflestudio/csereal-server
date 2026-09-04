package com.wafflestudio.csereal.core.conference.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.conference.database.ConferenceEntity
import com.wafflestudio.csereal.core.conference.database.ConferencePageEntity
import com.wafflestudio.csereal.core.conference.database.ConferencePageRepository
import com.wafflestudio.csereal.core.conference.database.ConferenceRepository
import com.wafflestudio.csereal.core.conference.dto.ConferenceDto
import com.wafflestudio.csereal.core.conference.dto.ConferenceModifyRequest
import com.wafflestudio.csereal.core.conference.dto.ConferencePage
import com.wafflestudio.csereal.core.research.database.ResearchSearchEntity
import com.wafflestudio.csereal.core.research.service.ResearchSearchService
import com.wafflestudio.csereal.core.user.service.UserService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface ConferenceService {
    fun getConferencePage(): ConferencePage
    fun modifyConferences(conferenceModifyRequest: ConferenceModifyRequest): ConferencePage
}

@Service
@Transactional
class ConferenceServiceImpl(
    private val conferencePageRepository: ConferencePageRepository,
    private val conferenceRepository: ConferenceRepository,
    private val researchSearchService: ResearchSearchService,
    private val userService: UserService
) : ConferenceService {

    @Transactional(readOnly = true)
    override fun getConferencePage(): ConferencePage {
        val conferencePage = conferencePageRepository.findAll()[0]
        return ConferencePage.of(conferencePage)
    }

    @Transactional
    override fun modifyConferences(
        conferenceModifyRequest: ConferenceModifyRequest
    ): ConferencePage {
        val user = userService.getLoginUser()

        val conferencePage = conferencePageRepository.findAll()[0]

        val newConferenceList = conferenceModifyRequest.newConferenceList.map {
            createConferenceWithoutSave(it, conferencePage)
        }

        val modifiedConferenceList = conferenceModifyRequest.modifiedConferenceList.map {
            modifyConferenceWithoutSave(it)
        }

        val deleteConferenceList = conferenceModifyRequest.deleteConferenceIdList.map {
            deleteConference(it, conferencePage)
        }

        conferencePage.author = user

        return ConferencePage.of(conferencePage)
    }

    @Transactional
    fun createConferenceWithoutSave(
        conferenceDto: ConferenceDto,
        conferencePage: ConferencePageEntity
    ): ConferenceEntity {
        val language = LanguageType.makeStringToLanguageType(conferenceDto.language)
        val newConference = ConferenceEntity.of(
            language,
            conferenceDto,
            conferencePage
        )
        conferencePage.conferences.add(newConference)

        newConference.researchSearch = ResearchSearchEntity.create(newConference)

        return newConference
    }

    @Transactional
    fun modifyConferenceWithoutSave(
        conferenceDto: ConferenceDto
    ): ConferenceEntity {
        val id = conferenceDto.id ?: throw CserealException(ErrorCode.CONFERENCE_NOT_FOUND)
        val conferenceEntity = conferenceRepository.findByIdOrNull(id)
            ?: throw CserealException(ErrorCode.CONFERENCE_NOT_FOUND, mapOf("id" to id))

        conferenceEntity.update(conferenceDto)

        conferenceEntity.researchSearch?.update(conferenceEntity)
            ?: let {
                conferenceEntity.researchSearch = ResearchSearchEntity.create(conferenceEntity)
            }

        return conferenceEntity
    }

    @Transactional
    fun deleteConference(
        id: Long,
        conferencePage: ConferencePageEntity
    ) = conferenceRepository.findByIdOrNull(id)
        ?.let {
            it.isDeleted = true
            conferencePage.conferences.remove(it)

            it.researchSearch?.let {
                researchSearchService.deleteResearchSearch(it)
            }
            it.researchSearch = null
        }
}
