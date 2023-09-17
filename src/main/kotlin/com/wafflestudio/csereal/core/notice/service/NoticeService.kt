package com.wafflestudio.csereal.core.notice.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import com.wafflestudio.csereal.core.notice.database.*
import com.wafflestudio.csereal.core.notice.dto.*
import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.multipart.MultipartFile

interface NoticeService {
    fun searchNotice(
        tag: List<String>?,
        keyword: String?,
        pageable: Pageable,
        usePageBtn: Boolean,
        isStaff: Boolean
    ): NoticeSearchResponse

    fun readNotice(noticeId: Long): NoticeDto
    fun createNotice(request: NoticeDto, attachments: List<MultipartFile>?): NoticeDto
    fun updateNotice(
        noticeId: Long,
        request: NoticeDto,
        newAttachments: List<MultipartFile>?,
    ): NoticeDto

    fun deleteNotice(noticeId: Long)
    fun unpinManyNotices(idList: List<Long>)
    fun deleteManyNotices(idList: List<Long>)
    fun enrollTag(tagName: String)
}

@Service
class NoticeServiceImpl(
    private val noticeRepository: NoticeRepository,
    private val tagInNoticeRepository: TagInNoticeRepository,
    private val noticeTagRepository: NoticeTagRepository,
    private val attachmentService: AttachmentService,
) : NoticeService {

    @Transactional(readOnly = true)
    override fun searchNotice(
        tag: List<String>?,
        keyword: String?,
        pageable: Pageable,
        usePageBtn: Boolean,
        isStaff: Boolean
    ): NoticeSearchResponse {
        return noticeRepository.searchNotice(tag, keyword, pageable, usePageBtn, isStaff)
    }

    @Transactional(readOnly = true)
    override fun readNotice(noticeId: Long): NoticeDto {
        val notice = noticeRepository.findByIdOrNull(noticeId)
            ?: throw CserealException.Csereal404("존재하지 않는 공지사항입니다.(noticeId: $noticeId)")

        if (notice.isDeleted) throw CserealException.Csereal404("삭제된 공지사항입니다.(noticeId: $noticeId)")

        val attachmentResponses = attachmentService.createAttachmentResponses(notice.attachments)

        val prevNotice = noticeRepository.findFirstByCreatedAtLessThanOrderByCreatedAtDesc(notice.createdAt!!)
        val nextNotice = noticeRepository.findFirstByCreatedAtGreaterThanOrderByCreatedAtAsc(notice.createdAt!!)

        return NoticeDto.of(notice, attachmentResponses, prevNotice, nextNotice)
    }

    @Transactional
    override fun createNotice(request: NoticeDto, attachments: List<MultipartFile>?): NoticeDto {
        val user = RequestContextHolder.getRequestAttributes()?.getAttribute(
            "loggedInUser",
            RequestAttributes.SCOPE_REQUEST
        ) as UserEntity

        val newNotice = NoticeEntity(
            title = request.title,
            titleForMain = request.titleForMain,
            description = request.description,
            plainTextDescription = cleanTextFromHtml(request.description),
            isPrivate = request.isPrivate,
            isPinned = request.isPinned,
            isImportant = request.isImportant,
            author = user
        )

        for (tag in request.tags) {
            val tagEnum = TagInNoticeEnum.getTagEnum(tag)
            val tagEntity = tagInNoticeRepository.findByName(tagEnum)
            NoticeTagEntity.createNoticeTag(newNotice, tagEntity)
        }

        if (attachments != null) {
            attachmentService.uploadAllAttachments(newNotice, attachments)
        }

        if (request.isImportant && request.titleForMain.isNullOrEmpty()) {
            throw CserealException.Csereal400("중요 제목이 입력되어야 합니다")
        }

        noticeRepository.save(newNotice)

        val attachmentResponses = attachmentService.createAttachmentResponses(newNotice.attachments)

        return NoticeDto.of(newNotice, attachmentResponses)

    }

    @Transactional
    override fun updateNotice(
        noticeId: Long,
        request: NoticeDto,
        newAttachments: List<MultipartFile>?
    ): NoticeDto {
        val notice: NoticeEntity = noticeRepository.findByIdOrNull(noticeId)
            ?: throw CserealException.Csereal404("존재하지 않는 공지사항입니다.(noticeId: $noticeId)")
        if (notice.isDeleted) throw CserealException.Csereal404("삭제된 공지사항입니다.(noticeId: $noticeId)")

        notice.update(request)

        attachmentService.deleteAttachments(request.deleteIds)

        if (newAttachments != null) {
            attachmentService.uploadAllAttachments(notice, newAttachments)
        }

        val oldTags = notice.noticeTags.map { it.tag.name }

        val tagsToRemove = oldTags - request.tags.map { TagInNoticeEnum.getTagEnum(it) }
        val tagsToAdd = request.tags.map { TagInNoticeEnum.getTagEnum(it) } - oldTags

        for (tagEnum in tagsToRemove) {
            val tagId = tagInNoticeRepository.findByName(tagEnum).id
            notice.noticeTags.removeIf { it.tag.name == tagEnum }
            noticeTagRepository.deleteByNoticeIdAndTagId(noticeId, tagId)
        }

        for (tagEnum in tagsToAdd) {
            val tagId = tagInNoticeRepository.findByName(tagEnum)
            NoticeTagEntity.createNoticeTag(notice, tagId)
        }

        val attachmentResponses = attachmentService.createAttachmentResponses(notice.attachments)

        return NoticeDto.of(notice, attachmentResponses)
    }

    @Transactional
    override fun deleteNotice(noticeId: Long) {
        val notice: NoticeEntity = noticeRepository.findByIdOrNull(noticeId)
            ?: throw CserealException.Csereal404("존재하지 않는 공지사항입니다.(noticeId: $noticeId)")

        notice.isDeleted = true

    }

    @Transactional
    override fun unpinManyNotices(idList: List<Long>) {
        for (noticeId in idList) {
            val notice: NoticeEntity = noticeRepository.findByIdOrNull(noticeId)
                ?: throw CserealException.Csereal404("존재하지 않는 공지사항을 입력하였습니다.(noticeId: $noticeId)")
            notice.isPinned = false
        }
    }

    @Transactional
    override fun deleteManyNotices(idList: List<Long>) {
        for (noticeId in idList) {
            val notice: NoticeEntity = noticeRepository.findByIdOrNull(noticeId)
                ?: throw CserealException.Csereal404("존재하지 않는 공지사항을 입력하였습니다.(noticeId: $noticeId)")
            notice.isDeleted = true
        }
    }

    override fun enrollTag(tagName: String) {
        val newTag = TagInNoticeEntity(
            name = TagInNoticeEnum.getTagEnum(tagName)
        )
        tagInNoticeRepository.save(newTag)
    }


}
