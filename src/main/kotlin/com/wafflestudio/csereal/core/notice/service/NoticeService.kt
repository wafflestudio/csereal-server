package com.wafflestudio.csereal.core.notice.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.notice.database.*
import com.wafflestudio.csereal.core.notice.dto.*
import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.multipart.MultipartFile

interface NoticeService {
    fun searchNotice(tag: List<String>?, keyword: String?, pageNum: Long): NoticeSearchResponse
    fun readNotice(noticeId: Long, tag: List<String>?, keyword: String?): NoticeDto
    fun createNotice(request: NoticeDto, attachments: List<MultipartFile>?): NoticeDto
    fun updateNotice(noticeId: Long, request: NoticeDto, attachments: List<MultipartFile>?): NoticeDto
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
    private val userRepository: UserRepository,
    private val attachmentService: AttachmentService,
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

        val attachmentResponses = attachmentService.createAttachmentResponses(notice.attachments)

        val prevNext = noticeRepository.findPrevNextId(noticeId, tag, keyword)

        return NoticeDto.of(notice, attachmentResponses, prevNext)
    }

    @Transactional
    override fun createNotice(request: NoticeDto, attachments: List<MultipartFile>?): NoticeDto {
        /*
        var user = RequestContextHolder.getRequestAttributes()?.getAttribute(
            "loggedInUser",
            RequestAttributes.SCOPE_REQUEST
        ) as UserEntity?

        if (user == null) {
            val oidcUser = SecurityContextHolder.getContext().authentication.principal as OidcUser
            val username = oidcUser.idToken.getClaim<String>("username")

            user = userRepository.findByUsername(username) ?: throw CserealException.Csereal404("재로그인이 필요합니다.")
        }

         */

        val newNotice = NoticeEntity(
            title = request.title,
            description = request.description,
            isPublic = request.isPublic,
            isPinned = request.isPinned,
            isImportant = request.isImportant,
           // author = user
        )

        for (tagName in request.tags) {
            val tag = tagInNoticeRepository.findByName(tagName) ?: throw CserealException.Csereal404("해당하는 태그가 없습니다")
            NoticeTagEntity.createNoticeTag(newNotice, tag)
        }

        if(attachments != null) {
            attachmentService.uploadAllAttachments(newNotice, attachments)
        }

        noticeRepository.save(newNotice)

        val attachmentResponses = attachmentService.createAttachmentResponses(newNotice.attachments)

        return NoticeDto.of(newNotice, attachmentResponses, null)

    }

    @Transactional
    override fun updateNotice(noticeId: Long, request: NoticeDto, attachments: List<MultipartFile>?): NoticeDto {
        val notice: NoticeEntity = noticeRepository.findByIdOrNull(noticeId)
            ?: throw CserealException.Csereal404("존재하지 않는 공지사항입니다.(noticeId: $noticeId)")
        if (notice.isDeleted) throw CserealException.Csereal404("삭제된 공지사항입니다.(noticeId: $noticeId)")

        notice.update(request)

        if(attachments != null) {
            notice.attachments.clear()
            attachmentService.uploadAllAttachments(notice, attachments)
        } else {
            notice.attachments.clear()
        }

        val oldTags = notice.noticeTags.map { it.tag.name }

        val tagsToRemove = oldTags - request.tags
        val tagsToAdd = request.tags - oldTags

        for (tagName in tagsToRemove) {
            val tagId = tagInNoticeRepository.findByName(tagName)!!.id
            notice.noticeTags.removeIf { it.tag.name == tagName }
            noticeTagRepository.deleteByNoticeIdAndTagId(noticeId, tagId)
        }

        for (tagName in tagsToAdd) {
            val tag = tagInNoticeRepository.findByName(tagName) ?: throw CserealException.Csereal404("해당하는 태그가 없습니다")
            NoticeTagEntity.createNoticeTag(notice, tag)
        }

        val attachmentResponses = attachmentService.createAttachmentResponses(notice.attachments)

        return NoticeDto.of(notice, attachmentResponses, null)


    }

    @Transactional
    override fun deleteNotice(noticeId: Long) {
        val notice: NoticeEntity = noticeRepository.findByIdOrNull(noticeId)
            ?: throw CserealException.Csereal404("존재하지 않는 공지사항입니다.(noticeId: $noticeId)")

        notice.isDeleted = true

    }

    @Transactional
    override fun unpinManyNotices(idList: List<Long>) {
        for(noticeId in idList) {
            val notice: NoticeEntity = noticeRepository.findByIdOrNull(noticeId)
                ?: throw CserealException.Csereal404("존재하지 않는 공지사항을 입력하였습니다.(noticeId: $noticeId)")
            notice.isPinned = false
        }
    }
    @Transactional
    override fun deleteManyNotices(idList: List<Long>) {
        for(noticeId in idList) {
            val notice: NoticeEntity = noticeRepository.findByIdOrNull(noticeId)
                ?: throw CserealException.Csereal404("존재하지 않는 공지사항을 입력하였습니다.(noticeId: $noticeId)")
            notice.isDeleted = true
        }
    }

    override fun enrollTag(tagName: String) {
        val newTag = TagInNoticeEntity(
            name = tagName
        )
        tagInNoticeRepository.save(newTag)
    }

    //TODO: 이미지 등록, 글쓴이 함께 조회
}
