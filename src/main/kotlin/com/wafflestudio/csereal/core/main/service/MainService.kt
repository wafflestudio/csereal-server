package com.wafflestudio.csereal.core.main.service

import com.wafflestudio.csereal.core.main.database.MainRepository
import com.wafflestudio.csereal.core.main.dto.MainResponse
import com.wafflestudio.csereal.core.main.dto.NoticesResponse
import com.wafflestudio.csereal.core.main.event.RefreshSearchEvent
import com.wafflestudio.csereal.core.notice.database.TagInNoticeEnum
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface MainService {
    fun readMain(): MainResponse
    fun refreshSearch()
}

@Service
class MainServiceImpl(
    private val mainRepository: MainRepository,
    private val eventPublisher: ApplicationEventPublisher
) : MainService {
    @Transactional(readOnly = true)
    override fun readMain(): MainResponse {
        val slides = mainRepository.readMainSlide()

        val noticeTotal = mainRepository.readMainNoticeTotal()
        val noticeScholarship = mainRepository.readMainNoticeTag(TagInNoticeEnum.SCHOLARSHIP)
        val noticeUndergraduate = mainRepository.readMainNoticeTag(TagInNoticeEnum.UNDERGRADUATE)
        val noticeGraduate = mainRepository.readMainNoticeTag(TagInNoticeEnum.GRADUATE)
        val notices = NoticesResponse(noticeTotal, noticeScholarship, noticeUndergraduate, noticeGraduate)

        val importants = mainRepository.readMainImportant()

        return MainResponse(slides, notices, importants)
    }

    override fun refreshSearch() {
        eventPublisher.publishEvent(RefreshSearchEvent())
    }
}
