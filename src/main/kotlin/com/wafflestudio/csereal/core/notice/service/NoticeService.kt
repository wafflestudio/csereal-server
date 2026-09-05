package com.wafflestudio.csereal.core.notice.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.enums.ContentSearchSortType
import com.wafflestudio.csereal.common.search.SearchListService
import com.wafflestudio.csereal.common.search.SearchType
import com.wafflestudio.csereal.common.utils.isCurrentUserStaff
import com.wafflestudio.csereal.core.notice.api.req.CreateNoticeReq
import com.wafflestudio.csereal.core.notice.api.req.UpdateNoticeReq
import com.wafflestudio.csereal.core.notice.database.*
import com.wafflestudio.csereal.core.notice.dto.*
import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import com.wafflestudio.csereal.core.user.service.UserService
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface NoticeService {
    fun searchNotice(
        tag: List<String>?,
        keyword: String?,
        pageable: Pageable,
        usePageBtn: Boolean,
        sortBy: ContentSearchSortType
    ): NoticeSearchResponse

    fun searchTotalNotice(keyword: String, number: Int, stringLength: Int): NoticeTotalSearchResponse

    fun readNotice(noticeId: Long): NoticeResponse
    fun createNotice(request: CreateNoticeReq, attachments: List<MultipartFile>?): NoticeResponse
    fun updateNotice(
        noticeId: Long,
        request: UpdateNoticeReq,
        newAttachments: List<MultipartFile>?
    ): NoticeResponse

    fun deleteNotice(noticeId: Long)
    fun unpinManyNotices(idList: List<Long>)
    fun deleteManyNotices(idList: List<Long>)
    fun enrollTag(tagName: String)
}

@Service
class NoticeServiceImpl(
    private val noticeRepository: NoticeRepository,
    private val searchListService: SearchListService,
    private val tagInNoticeRepository: TagInNoticeRepository,
    private val noticeTagRepository: NoticeTagRepository,
    private val attachmentService: AttachmentService,
    private val userService: UserService
) : NoticeService {

    /**
     * 키워드가 있으면 ES 가 거르고 정렬하고 페이지를 나눈다. 화면에 그릴 값은 그 id 로
     * DB 에서 읽는다. 키워드 없이 태그만 훑을 때는 색인을 거칠 이유가 없다.
     */
    @Transactional(readOnly = true)
    override fun searchNotice(
        tag: List<String>?,
        keyword: String?,
        pageable: Pageable,
        usePageBtn: Boolean,
        sortBy: ContentSearchSortType
    ): NoticeSearchResponse {
        val isStaff = isCurrentUserStaff()
        if (keyword.isNullOrEmpty()) {
            return noticeRepository.browseNotice(tag, pageable, usePageBtn, isStaff)
        }

        val page = searchListService.searchIds(
            type = SearchType.NOTICE,
            keyword = keyword,
            tags = tag.orEmpty().map { TagInNoticeEnum.getTagEnum(it).name },
            isStaff = isStaff,
            offset = pageable.offset,
            size = pageable.pageSize
        )
        return NoticeSearchResponse(page.total, noticeRepository.findSearchDtosByIds(page.ids))
    }

    @Transactional(readOnly = true)
    override fun searchTotalNotice(
        keyword: String,
        number: Int,
        stringLength: Int
    ) = noticeRepository.totalSearchNotice(keyword, number, stringLength, isCurrentUserStaff())

    @Transactional(readOnly = true)
    override fun readNotice(noticeId: Long): NoticeResponse {
        val notice = noticeRepository.findByIdOrNull(noticeId)
            ?: throw CserealException(ErrorCode.NOTICE_NOT_FOUND, mapOf("noticeId" to noticeId))

        if (notice.isPrivate && !isCurrentUserStaff()) throw CserealException(ErrorCode.PRIVATE_POST)

        val attachmentResponses = attachmentService.createAttachmentResponses(notice.attachments)

        val prevNotice =
            noticeRepository.findFirstByIsPrivateFalseAndCreatedAtLessThanOrderByCreatedAtDesc(
                notice.createdAt!!
            )
        val nextNotice =
            noticeRepository.findFirstByIsPrivateFalseAndCreatedAtGreaterThanOrderByCreatedAtAsc(
                notice.createdAt!!
            )

        return NoticeResponse.of(notice, attachmentResponses, prevNotice, nextNotice)
    }

    @Transactional
    override fun createNotice(request: CreateNoticeReq, attachments: List<MultipartFile>?): NoticeResponse {
        val newNotice = NoticeEntity.of(request, userService.getLoginUser())

        for (tag in request.tags) {
            val tagEnum = TagInNoticeEnum.getTagEnum(tag)
            val tagEntity = tagInNoticeRepository.findByName(tagEnum)
            NoticeTagEntity.createNoticeTag(newNotice, tagEntity)
        }

        if (attachments != null) {
            attachmentService.uploadAllAttachments(newNotice, attachments)
        }

        noticeRepository.save(newNotice)

        val attachmentResponses = attachmentService.createAttachmentResponses(newNotice.attachments)

        return NoticeResponse.of(newNotice, attachmentResponses)
    }

    @Transactional
    override fun updateNotice(
        noticeId: Long,
        request: UpdateNoticeReq,
        newAttachments: List<MultipartFile>?
    ): NoticeResponse {
        val notice: NoticeEntity = noticeRepository.findByIdOrNull(noticeId)
            ?: throw CserealException(ErrorCode.NOTICE_NOT_FOUND, mapOf("noticeId" to noticeId))

        notice.update(request)

        attachmentService.syncAttachments(notice, request.attachmentIds, newAttachments)

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

        return NoticeResponse.of(notice, attachmentResponses)
    }

    @Transactional
    override fun deleteNotice(noticeId: Long) {
        noticeRepository.findByIdOrNull(noticeId)
            ?: throw CserealException(ErrorCode.NOTICE_NOT_FOUND, mapOf("noticeId" to noticeId))

        noticeRepository.deleteById(noticeId)
    }

    @Transactional
    override fun unpinManyNotices(idList: List<Long>) {
        for (noticeId in idList) {
            val notice: NoticeEntity = noticeRepository.findByIdOrNull(noticeId)
                ?: throw CserealException(ErrorCode.NOTICE_NOT_FOUND, mapOf("noticeId" to noticeId))
            notice.isPinned = false
        }
    }

    @Transactional
    override fun deleteManyNotices(idList: List<Long>) {
        for (noticeId in idList) {
            noticeRepository.findByIdOrNull(noticeId)
                ?: throw CserealException(ErrorCode.NOTICE_NOT_FOUND, mapOf("noticeId" to noticeId))
            noticeRepository.deleteById(noticeId)
        }
    }

    override fun enrollTag(tagName: String) {
        val newTag = TagInNoticeEntity(
            name = TagInNoticeEnum.getTagEnum(tagName)
        )
        tagInNoticeRepository.save(newTag)
    }
}
