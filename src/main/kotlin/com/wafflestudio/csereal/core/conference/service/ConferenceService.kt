package com.wafflestudio.csereal.core.conference.service

import com.wafflestudio.csereal.core.conference.database.ConferencePageRepository
import com.wafflestudio.csereal.core.conference.dto.ConferenceDto
import com.wafflestudio.csereal.core.conference.dto.ConferencePage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


interface ConferenceService {
    fun getConferencePage(): ConferencePage
}

@Service
@Transactional
class ConferenceServiceImpl(
    private val conferencePageRepository: ConferencePageRepository
) : ConferenceService {

    @Transactional(readOnly = true)
    override fun getConferencePage(): ConferencePage {
        val conferencePage = conferencePageRepository.findAll()[0]
        return ConferencePage.of(conferencePage)
    }

}
