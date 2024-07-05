package com.wafflestudio.csereal.core.main.service

import com.wafflestudio.csereal.core.main.database.MainRepository
import com.wafflestudio.csereal.core.main.dto.MainImportantResponse
import com.wafflestudio.csereal.core.main.dto.MainResponse
import com.wafflestudio.csereal.core.main.dto.NoticesResponse
import com.wafflestudio.csereal.core.main.event.RefreshSearchEvent
import com.wafflestudio.csereal.core.news.database.NewsRepository
import com.wafflestudio.csereal.core.notice.database.NoticeRepository
import com.wafflestudio.csereal.core.notice.database.TagInNoticeEnum
import com.wafflestudio.csereal.core.seminar.database.SeminarRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface MainService {
    fun readMain(importantCnt: Int?): MainResponse
    fun refreshSearch()
    fun readMainImportant(cnt: Int? = null): List<MainImportantResponse>
}

@Service
class MainServiceImpl(
    private val mainRepository: MainRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val noticeRepository: NoticeRepository,
    private val seminarRepository: SeminarRepository,
    private val newsRepository: NewsRepository
) : MainService {
    @Transactional(readOnly = true)
    override fun readMain(importantCnt: Int?): MainResponse {
        val slides = mainRepository.readMainSlide()

        val noticeTotal = mainRepository.readMainNoticeTotal()
        val noticeScholarship = mainRepository.readMainNoticeTag(TagInNoticeEnum.SCHOLARSHIP)
        val noticeUndergraduate = mainRepository.readMainNoticeTag(TagInNoticeEnum.UNDERGRADUATE)
        val noticeGraduate = mainRepository.readMainNoticeTag(TagInNoticeEnum.GRADUATE)
        val notices = NoticesResponse(noticeTotal, noticeScholarship, noticeUndergraduate, noticeGraduate)

        val importants = readMainImportant(importantCnt)

        return MainResponse(slides, notices, importants)
    }

    @Transactional(readOnly = true)
    override fun readMainImportant(cnt: Int?): List<MainImportantResponse> =
        mutableListOf<MainImportantResponse>().apply {
            addAll(noticeRepository.findImportantNotice(cnt))
            addAll(seminarRepository.findImportantSeminar(cnt))
            addAll(newsRepository.findImportantNews(cnt))
            sortByDescending { it.createdAt }
        }.let {
            if (cnt != null) it.take(cnt) else it
        }

    override fun refreshSearch() {
        eventPublisher.publishEvent(RefreshSearchEvent())
    }
}
