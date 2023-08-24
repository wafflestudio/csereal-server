package com.wafflestudio.csereal.core.main.service

import com.wafflestudio.csereal.core.main.database.MainRepository
import com.wafflestudio.csereal.core.main.dto.MainResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface MainService {
    fun readMain() : MainResponse
}

@Service
class MainServiceImpl(
    private val mainRepository: MainRepository
) : MainService {
    @Transactional(readOnly = true)
    override fun readMain(): MainResponse {
        val slide = mainRepository.readMainSlide()
        val noticeTotal = mainRepository.readMainNoticeTotal()
        val noticeAdmissions = mainRepository.readMainNoticeTag("admissions")
        val noticeUndergraduate = mainRepository.readMainNoticeTag("undergraduate")
        val noticeGraduate = mainRepository.readMainNoticeTag("graduate")
        return MainResponse(slide, noticeTotal, noticeAdmissions, noticeUndergraduate, noticeGraduate)
    }
}