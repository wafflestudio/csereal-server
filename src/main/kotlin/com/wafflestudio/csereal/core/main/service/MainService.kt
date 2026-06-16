package com.wafflestudio.csereal.core.main.service

import com.wafflestudio.csereal.common.enums.ContentSearchSortType
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.about.service.AboutService
import com.wafflestudio.csereal.core.academics.service.AcademicsSearchService
import com.wafflestudio.csereal.core.admissions.service.AdmissionsService
import com.wafflestudio.csereal.core.main.dto.SearchV3Response
import com.wafflestudio.csereal.core.main.dto.TotalSearchResponse
import com.wafflestudio.csereal.core.main.database.MainRepository
import com.wafflestudio.csereal.core.main.dto.MainImportantResponse
import com.wafflestudio.csereal.core.main.dto.MainResponse
import com.wafflestudio.csereal.core.main.dto.NoticesResponse
import com.wafflestudio.csereal.core.main.event.RefreshSearchEvent
import com.wafflestudio.csereal.core.member.service.MemberSearchService
import com.wafflestudio.csereal.core.news.database.NewsRepository
import com.wafflestudio.csereal.core.news.service.NewsService
import com.wafflestudio.csereal.core.notice.database.NoticeRepository
import com.wafflestudio.csereal.core.notice.database.TagInNoticeEnum
import com.wafflestudio.csereal.core.notice.service.NoticeService
import com.wafflestudio.csereal.core.research.service.ResearchSearchService
import com.wafflestudio.csereal.core.seminar.database.SeminarRepository
import com.wafflestudio.csereal.core.seminar.service.SeminarService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface MainService {
    fun readMain(importantCnt: Int?): MainResponse
    fun refreshSearch()
    fun readMainImportant(cnt: Int? = null): List<MainImportantResponse>
    fun totalSearch(
        keyword: String,
        number: Int,
        memberNumber: Int,
        stringLength: Int,
        language: LanguageType
    ): TotalSearchResponse

    fun searchV3(
        keyword: String,
        sections: Set<String>?,
        number: Int,
        memberNumber: Int,
        stringLength: Int,
        language: LanguageType
    ): SearchV3Response
}

@Service
class MainServiceImpl(
    private val mainRepository: MainRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val noticeRepository: NoticeRepository,
    private val seminarRepository: SeminarRepository,
    private val newsRepository: NewsRepository,
    private val aboutService: AboutService,
    private val noticeService: NoticeService,
    private val newsService: NewsService,
    private val seminarService: SeminarService,
    private val memberSearchService: MemberSearchService,
    private val researchSearchService: ResearchSearchService,
    private val admissionsService: AdmissionsService,
    private val academicsSearchService: AcademicsSearchService
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

    @Transactional(readOnly = true)
    override fun totalSearch(
        keyword: String,
        number: Int,
        memberNumber: Int,
        stringLength: Int,
        language: LanguageType
    ): TotalSearchResponse {
        val aboutResult = aboutService.searchTopAbout(
            keyword,
            language,
            number,
            stringLength
        )
        val noticeResult = noticeService.searchTotalNotice(
            keyword,
            number,
            stringLength
        )
        val newsResult = newsService.searchTotalNews(
            keyword,
            number,
            stringLength
        )
        val seminarResult = seminarService.searchSeminar(
            keyword,
            PageRequest.of(0, 10),
            usePageBtn = true,
            ContentSearchSortType.DATE
        )
        val memberResult = memberSearchService.searchTopMember(
            keyword,
            language,
            memberNumber
        )
        val researchResult = researchSearchService.searchTopResearch(
            keyword,
            language,
            number,
            stringLength
        )
        val admissionsResult = admissionsService.searchTopAdmission(
            keyword,
            language,
            number,
            stringLength
        )
        val academicsResult = academicsSearchService.searchTopAcademics(
            keyword,
            language,
            number,
            stringLength
        )

        return TotalSearchResponse(
            aboutResult = aboutResult,
            noticeResult = noticeResult,
            newsResult = newsResult,
            seminarResult = seminarResult,
            memberResult = memberResult,
            researchResult = researchResult,
            admissionsResult = admissionsResult,
            academicsResult = academicsResult
        )
    }

    @Transactional(readOnly = true)
    override fun searchV3(
        keyword: String,
        sections: Set<String>?,
        number: Int,
        memberNumber: Int,
        stringLength: Int,
        language: LanguageType
    ): SearchV3Response {
        // sections 미지정이면 전체. 지정 시 해당 섹션만 집계해 나머지는 null로 둔다.
        fun want(section: String) = sections.isNullOrEmpty() || section in sections

        return SearchV3Response(
            about = if (want("about")) {
                aboutService.searchTopAbout(keyword, language, number, stringLength)
            } else {
                null
            },
            notice = if (want("notice")) {
                noticeService.searchTotalNotice(keyword, number, stringLength)
            } else {
                null
            },
            news = if (want("news")) {
                newsService.searchTotalNews(keyword, number, stringLength)
            } else {
                null
            },
            seminar = if (want("seminar")) {
                seminarService.searchSeminar(
                    keyword,
                    PageRequest.of(0, 10),
                    usePageBtn = true,
                    ContentSearchSortType.DATE
                )
            } else {
                null
            },
            member = if (want("member")) {
                memberSearchService.searchTopMember(keyword, language, memberNumber)
            } else {
                null
            },
            research = if (want("research")) {
                researchSearchService.searchTopResearch(keyword, language, number, stringLength)
            } else {
                null
            },
            admissions = if (want("admissions")) {
                admissionsService.searchTopAdmission(keyword, language, number, stringLength)
            } else {
                null
            },
            academics = if (want("academics")) {
                academicsSearchService.searchTopAcademics(keyword, language, number, stringLength)
            } else {
                null
            }
        )
    }
}
