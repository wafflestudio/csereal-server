package com.wafflestudio.csereal.core.notice.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.notice.database.NoticeEntity
import com.wafflestudio.csereal.core.notice.database.NoticeRepository
import com.wafflestudio.csereal.core.notice.dto.CreateNoticeRequest
import com.wafflestudio.csereal.core.notice.dto.NoticeDto
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface NoticeService {
    fun readNotice(noticeId: Long): NoticeDto
    fun createNotice(request: CreateNoticeRequest): NoticeDto
}

@Service
class NoticeServiceImpl(
    private val noticeRepository: NoticeRepository,
) : NoticeService {

    @Transactional(readOnly = true)
    override fun readNotice(noticeId: Long): NoticeDto {
        val notice: NoticeEntity = noticeRepository.findByIdOrNull(noticeId)
            ?: throw CserealException.Csereal400("존재하지 않는 공지사항입니다.(noticeId: $noticeId)")
        return NoticeDto.of(notice)
    }

    @Transactional
    override fun createNotice(request: CreateNoticeRequest): NoticeDto {
        // TODO:"아직 날짜가 제대로 안 뜸"
        val newNotice = NoticeEntity(
            title = request.title,
            description = request.description,
        )

        noticeRepository.save(newNotice)

        return NoticeDto.of(newNotice)

    }
}