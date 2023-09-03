package com.wafflestudio.csereal.core.admin.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.admin.database.AdminRepository
import com.wafflestudio.csereal.core.admin.dto.SlideResponse
import com.wafflestudio.csereal.core.news.database.NewsEntity
import com.wafflestudio.csereal.core.news.database.NewsRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface AdminService {
    fun readAllSlides(pageNum: Long): List<SlideResponse>
    fun unSlideManyNews(idList: List<Long>)
}

@Service
class AdminServiceImpl(
    private val adminRepository: AdminRepository,
    private val newsRepository: NewsRepository
): AdminService {
    @Transactional
    override fun readAllSlides(pageNum: Long): List<SlideResponse> {
        return adminRepository.readAllSlides(pageNum)
    }

    @Transactional
    override fun unSlideManyNews(idList: List<Long>) {
        for(newsId in idList) {
            val news: NewsEntity = newsRepository.findByIdOrNull(newsId)
                ?: throw CserealException.Csereal404("존재하지 않는 새소식입니다.(newsId=$newsId)")
            news.isSlide = false
        }
    }
}

