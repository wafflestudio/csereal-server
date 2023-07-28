package com.wafflestudio.csereal.core.notice.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.notice.database.*
import com.wafflestudio.csereal.core.notice.dto.CreateNoticeRequest
import com.wafflestudio.csereal.core.notice.dto.NoticeDto
import com.wafflestudio.csereal.core.notice.dto.UpdateNoticeRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface NoticeService {
    fun readNotice(noticeId: Long): NoticeDto
    fun createNotice(request: CreateNoticeRequest): NoticeDto
    fun updateNotice(noticeId: Long, request: UpdateNoticeRequest): NoticeDto
    fun deleteNotice(noticeId: Long)
    fun enrollTag(tagName: String)
}

@Service
class NoticeServiceImpl(
    private val noticeRepository: NoticeRepository,
    private val tagRepository: TagRepository,
    private val noticeTagRepository: NoticeTagRepository
) : NoticeService {

    @Transactional(readOnly = true)
    override fun readNotice(noticeId: Long): NoticeDto {
        val notice: NoticeEntity = noticeRepository.findByIdOrNull(noticeId)
            ?: throw CserealException.Csereal400("존재하지 않는 공지사항입니다.(noticeId: $noticeId)")
        if (notice.isDeleted) throw CserealException.Csereal400("삭제된 공지사항입니다.(noticeId: $noticeId)")
        return NoticeDto.of(notice)
    }

    @Transactional
    override fun createNotice(request: CreateNoticeRequest): NoticeDto {
        val newNotice = NoticeEntity(
            title = request.title,
            description = request.description,
            isPublic = request.isPublic,
            isSlide = request.isSlide,
            isPinned = request.isPinned
        )

        for (tagId in request.tags) {
            val tag = tagRepository.findByIdOrNull(tagId) ?: throw CserealException.Csereal400("해당하는 태그가 없습니다")
            NoticeTagEntity.createNoticeTag(newNotice, tag)
        }

        noticeRepository.save(newNotice)

        return NoticeDto.of(newNotice)

    }

    @Transactional
    override fun updateNotice(noticeId: Long, request: UpdateNoticeRequest): NoticeDto {
        val notice: NoticeEntity = noticeRepository.findByIdOrNull(noticeId)
            ?: throw CserealException.Csereal400("존재하지 않는 공지사항입니다.(noticeId: $noticeId")
        if (notice.isDeleted) throw CserealException.Csereal400("삭제된 공지사항입니다.(noticeId: $noticeId")

        notice.title = request.title ?: notice.title
        notice.description = request.description ?: notice.description
        notice.isPublic = request.isPublic ?: notice.isPublic
        notice.isSlide = request.isSlide ?: notice.isSlide
        notice.isPinned = request.isPinned ?: notice.isPinned

        if (request.tags != null) {
            noticeTagRepository.deleteAllByNoticeId(noticeId)
            notice.noticeTags.clear()
            for (tagId in request.tags) {
                val tag = tagRepository.findByIdOrNull(tagId) ?: throw CserealException.Csereal400("해당하는 태그가 없습니다")
                NoticeTagEntity.createNoticeTag(notice, tag)
            }
        }



        return NoticeDto.of(notice)
    }

    @Transactional
    override fun deleteNotice(noticeId: Long) {
        val notice: NoticeEntity = noticeRepository.findByIdOrNull(noticeId)
            ?: throw CserealException.Csereal400("존재하지 않는 공지사항입니다.(noticeId: $noticeId)")

        notice.isDeleted = true

    }

    override fun enrollTag(tagName: String) {
        val newTag = TagEntity(
            name = tagName
        )
        tagRepository.save(newTag)
    }

    //TODO: 이미지 등록, 페이지네이션, 검색
}