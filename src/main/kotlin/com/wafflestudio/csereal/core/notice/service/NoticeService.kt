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
    fun readNotice(postId: Long): NoticeDto
    fun createNotice(request: CreateNoticeRequest): NoticeDto
}

@Service
class NoticeServiceImpl(
    private val noticeRepository: NoticeRepository,
) : NoticeService {

    @Transactional(readOnly = true)
    override fun readNotice(postId: Long): NoticeDto {
        val post: NoticeEntity = noticeRepository.findByIdOrNull(postId)
            ?: throw CserealException.Csereal400("존재하지 않는 질문 번호 입니다.(postId: $postId)")
        return NoticeDto.of(post)
    }

    @Transactional
    override fun createNotice(request: CreateNoticeRequest): NoticeDto {
        // TODO():"아직 날짜가 제대로 안 뜸"
        val newPost = NoticeEntity(
            title = request.title,
            description = request.description,
        )

        noticeRepository.save(newPost)

        return NoticeDto.of(newPost)

    }
}