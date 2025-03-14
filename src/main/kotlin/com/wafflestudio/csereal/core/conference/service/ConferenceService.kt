package com.wafflestudio.csereal.core.conference.service

import com.wafflestudio.csereal.common.CserealException
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
    fun migrateConferences(requestList: List<ConferenceDto>): List<ConferenceDto>
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
    override fun migrateConferences(requestList: List<ConferenceDto>): List<ConferenceDto> {
        val user = userService.getLoginUser()

        val list = mutableListOf<ConferenceDto>()
        val conferencePage = ConferencePageEntity.of(user)
        conferencePageRepository.save(conferencePage)
        for (request in requestList) {
            val enumLanguageType = LanguageType.makeStringToLanguageType(request.language)
            val conference = ConferenceEntity.of(enumLanguageType, request, conferencePage)

            conferenceRepository.save(conference)

            conferencePage.conferences.add(conference)

            conference.researchSearch = ResearchSearchEntity.create(conference)
            list.add(ConferenceDto.of(conference))
        }

        return list
    }

    @Transactional
    fun createConferenceWithoutSave(
        conferenceDto: ConferenceDto,
        conferencePage: ConferencePageEntity
    ): ConferenceEntity {
        val enumLanguageType = LanguageType.makeStringToLanguageType(conferenceDto.language)
        val newConference = ConferenceEntity.of(
            enumLanguageType,
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
        val conferenceEntity = conferenceRepository.findByIdOrNull(conferenceDto.id)
            ?: throw CserealException.Csereal404("Conference id:${conferenceDto.id} 가 존재하지 않습니다.")

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
