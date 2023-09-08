package com.wafflestudio.csereal.core.main.service

import com.wafflestudio.csereal.core.main.database.MainRepository
import com.wafflestudio.csereal.core.main.dto.MainResponse
import com.wafflestudio.csereal.core.main.dto.NoticesResponse
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
        val slides = mainRepository.readMainSlide()

        val noticeTotal = mainRepository.readMainNoticeTotal()
        val noticeScholarship = mainRepository.readMainNoticeTag("scholarship")
        val noticeUndergraduate = mainRepository.readMainNoticeTag("undergraduate")
        val noticeGraduate = mainRepository.readMainNoticeTag("graduate")
        val notices = NoticesResponse(noticeTotal, noticeScholarship, noticeUndergraduate, noticeGraduate)

        val importants = mainRepository.readMainImportant()

        return MainResponse(slides, notices, importants)
    }
}