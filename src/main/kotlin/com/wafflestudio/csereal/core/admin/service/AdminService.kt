package com.wafflestudio.csereal.core.admin.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.admin.dto.ImportantDto
import com.wafflestudio.csereal.core.admin.dto.ImportantResponse
import com.wafflestudio.csereal.core.admin.dto.AdminSlidesResponse
import com.wafflestudio.csereal.core.news.database.NewsRepository
import com.wafflestudio.csereal.core.news.service.NewsService
import com.wafflestudio.csereal.core.notice.database.NoticeRepository
import com.wafflestudio.csereal.core.seminar.database.SeminarRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface AdminService {
    fun readAllSlides(pageNum: Long, pageSize: Int): AdminSlidesResponse
    fun unSlideManyNews(request: List<Long>)
    fun readAllImportants(pageNum: Long): List<ImportantResponse>
    fun makeNotImportants(request: List<ImportantDto>)
}

@Service
class AdminServiceImpl(
    private val newsService: NewsService,
    private val noticeRepository: NoticeRepository,
    private val newsRepository: NewsRepository,
    private val seminarRepository: SeminarRepository
) : AdminService {
    @Transactional(readOnly = true)
    override fun readAllSlides(pageNum: Long, pageSize: Int): AdminSlidesResponse
        = newsService.readAllSlides(pageNum, pageSize)

    @Transactional
    override fun unSlideManyNews(request: List<Long>)
        = newsService.unSlideManyNews(request)

    // TODO: 각 도메인의 Service로 구현, Service method 이용하기
    @Transactional
    override fun readAllImportants(pageNum: Long): List<ImportantResponse> {
        val importantResponses: MutableList<ImportantResponse> = mutableListOf()
        noticeRepository.findAllByIsImportantTrueAndIsDeletedFalse().forEach {
            importantResponses.add(
                ImportantResponse(
                    id = it.id,
                    title = it.title,
                    createdAt = it.createdAt,
                    category = "notice"
                )
            )
        }

        newsRepository.findAllByIsImportantTrueAndIsDeletedFalse().forEach {
            importantResponses.add(
                ImportantResponse(
                    id = it.id,
                    title = it.title,
                    createdAt = it.createdAt,
                    category = "news"
                )
            )
        }

        seminarRepository.findAllByIsImportantTrueAndIsDeletedFalse().forEach {
            importantResponses.add(
                ImportantResponse(
                    id = it.id,
                    title = it.title,
                    createdAt = it.createdAt,
                    category = "seminar"
                )
            )
        }
        importantResponses.sortByDescending { it.createdAt }

        return importantResponses
    }

    // TODO: 각 도메인의 Service로 구현, Service method 이용하기
    @Transactional
    override fun makeNotImportants(request: List<ImportantDto>) {
        for (important in request) {
            when (important.category) {
                "notice" -> {
                    val notice = noticeRepository.findByIdOrNull(important.id)
                        ?: throw CserealException.Csereal404("해당하는 공지사항을 찾을 수 없습니다.(noticeId=${important.id})")
                    notice.isImportant = false
                }

                "news" -> {
                    val news = newsRepository.findByIdOrNull(important.id)
                        ?: throw CserealException.Csereal404("해당하는 새소식을 찾을 수 없습니다.(noticeId=${important.id})")
                    news.isImportant = false
                }

                "seminar" -> {
                    val seminar = seminarRepository.findByIdOrNull(important.id)
                        ?: throw CserealException.Csereal404("해당하는 세미나를 찾을 수 없습니다.(noticeId=${important.id})")
                    seminar.isImportant = false
                }
            }
        }
    }
}
