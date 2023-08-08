package com.wafflestudio.csereal.core.notice.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.notice.database.*
import com.wafflestudio.csereal.core.notice.dto.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface NoticeService {
    fun searchNotice(tag: List<String>?, keyword: String?, pageNum: Long): NoticeSearchResponse
    fun readNotice(noticeId: Long, tag: List<String>?, keyword: String?): NoticeDto
    fun createNotice(request: NoticeDto): NoticeDto
    fun updateNotice(noticeId: Long, request: NoticeDto): NoticeDto
    fun deleteNotice(noticeId: Long)
    fun enrollTag(tagName: String)
}

@Service
class NoticeServiceImpl(
    private val noticeRepository: NoticeRepository,
    private val tagInNoticeRepository: TagInNoticeRepository,
    private val noticeTagRepository: NoticeTagRepository
) : NoticeService {

    @Transactional(readOnly = true)
    override fun searchNotice(
        tag: List<String>?,
        keyword: String?,
        pageNum: Long
        ): NoticeSearchResponse {
            return noticeRepository.searchNotice(tag, keyword, pageNum)
        }

    @Transactional(readOnly = true)
    override fun readNotice(
        noticeId: Long,
        tag: List<String>?,
        keyword: String?
    ): NoticeDto {
        val notice: NoticeEntity = noticeRepository.findByIdOrNull(noticeId)
            ?: throw CserealException.Csereal404("존재하지 않는 공지사항입니다.(noticeId: $noticeId)")
        
        if (notice.isDeleted) throw CserealException.Csereal404("삭제된 공지사항입니다.(noticeId: $noticeId)")

        val prevNext = noticeRepository.findPrevNextId(noticeId, tag, keyword)

        return NoticeDto.of(notice, prevNext)
    }

    @Transactional
    override fun createNotice(request: NoticeDto): NoticeDto {
        val newNotice = NoticeEntity(
            title = request.title,
            description = request.description,
            isPublic = request.isPublic,
            isSlide = request.isSlide,
            isPinned = request.isPinned,
        )

        for (tagName in request.tags) {
            val tag = tagInNoticeRepository.findByName(tagName) ?: throw CserealException.Csereal404("해당하는 태그가 없습니다")
            NoticeTagEntity.createNoticeTag(newNotice, tag)
        }

        noticeRepository.save(newNotice)

        return NoticeDto.of(newNotice, null)

    }

    @Transactional
    override fun updateNotice(noticeId: Long, request: NoticeDto): NoticeDto {
        val notice: NoticeEntity = noticeRepository.findByIdOrNull(noticeId)
            ?: throw CserealException.Csereal404("존재하지 않는 공지사항입니다.(noticeId: $noticeId)")
        if (notice.isDeleted) throw CserealException.Csereal404("삭제된 공지사항입니다.(noticeId: $noticeId)")

        notice.update(request)

        val oldTags = notice.noticeTags.map { it.tag.name }

        val tagsToRemove = oldTags - request.tags
        val tagsToAdd = request.tags - oldTags

        for(tagName in tagsToRemove) {
            val tagId = tagInNoticeRepository.findByName(tagName)!!.id
            notice.noticeTags.removeIf { it.tag.name == tagName }
            noticeTagRepository.deleteByNoticeIdAndTagId(noticeId, tagId)
        }

        for(tagName in tagsToAdd) {
            val tag = tagInNoticeRepository.findByName(tagName) ?: throw CserealException.Csereal404("해당하는 태그가 없습니다")
            NoticeTagEntity.createNoticeTag(notice, tag)
        }

        return NoticeDto.of(notice, null)




    }

    @Transactional
    override fun deleteNotice(noticeId: Long) {
        val notice: NoticeEntity = noticeRepository.findByIdOrNull(noticeId)
            ?: throw CserealException.Csereal404("존재하지 않는 공지사항입니다.(noticeId: $noticeId)")

        notice.isDeleted = true

    }

    override fun enrollTag(tagName: String) {
        val newTag = TagInNoticeEntity(
            name = tagName
        )
        tagInNoticeRepository.save(newTag)
    }

    //TODO: 이미지 등록, 글쓴이 함께 조회
}