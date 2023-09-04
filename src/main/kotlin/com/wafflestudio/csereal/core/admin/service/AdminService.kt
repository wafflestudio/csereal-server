package com.wafflestudio.csereal.core.admin.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.admin.database.AdminRepository
import com.wafflestudio.csereal.core.admin.dto.ImportantDto
import com.wafflestudio.csereal.core.admin.dto.ImportantRequest
import com.wafflestudio.csereal.core.admin.dto.ImportantResponse
import com.wafflestudio.csereal.core.admin.dto.SlideResponse
import com.wafflestudio.csereal.core.news.database.NewsEntity
import com.wafflestudio.csereal.core.news.database.NewsRepository
import com.wafflestudio.csereal.core.notice.database.NoticeRepository
import com.wafflestudio.csereal.core.seminar.database.SeminarRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

interface AdminService {
    fun readAllSlides(pageNum: Long): List<SlideResponse>
    fun unSlideManyNews(request: List<Long>)
    fun readAllImportants(pageNum: Long): List<ImportantResponse>
    fun makeNotImportants(request: List<ImportantDto>)
}

@Service
class AdminServiceImpl(
    private val adminRepository: AdminRepository,
    private val noticeRepository: NoticeRepository,
    private val newsRepository: NewsRepository,
    private val seminarRepository: SeminarRepository,
) : AdminService {
    @Transactional
    override fun readAllSlides(pageNum: Long): List<SlideResponse> {
        return adminRepository.readAllSlides(pageNum)
    }

    @Transactional
    override fun unSlideManyNews(request: List<Long>) {
        for (newsId in request) {
            val news: NewsEntity = newsRepository.findByIdOrNull(newsId)
                ?: throw CserealException.Csereal404("존재하지 않는 새소식입니다.(newsId=$newsId)")
            news.isSlide = false
        }
    }

    @Transactional
    override fun readAllImportants(pageNum: Long): List<ImportantResponse> {
        val importantResponses: MutableList<ImportantResponse> = mutableListOf()
        noticeRepository.findAllByIsImportant(true).forEach {
            importantResponses.add(
                ImportantResponse(
                    id = it.id,
                    title = it.title,
                    createdAt = it.createdAt,
                    category = "notice"
                )
            )
        }

        newsRepository.findAllByIsImportant(true).forEach {
            importantResponses.add(
                ImportantResponse(
                    id = it.id,
                    title = it.title,
                    createdAt = it.createdAt,
                    category = "news"
                )
            )
        }

        seminarRepository.findAllByIsImportant(true).forEach {
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

    @Transactional
    override fun makeNotImportants(request: List<ImportantDto>) {
        for(important in request) {
            when(important.category) {
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
