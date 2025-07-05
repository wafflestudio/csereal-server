package com.wafflestudio.csereal.core.seminar.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.enums.ContentSearchSortType
import com.wafflestudio.csereal.common.utils.isCurrentUserStaff
import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import com.wafflestudio.csereal.core.seminar.database.SeminarEntity
import com.wafflestudio.csereal.core.seminar.database.SeminarRepository
import com.wafflestudio.csereal.core.seminar.dto.SeminarDto
import com.wafflestudio.csereal.core.seminar.dto.SeminarSearchResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface SeminarService {
    fun searchSeminar(
        keyword: String?,
        pageable: Pageable,
        usePageBtn: Boolean,
        sortBy: ContentSearchSortType
    ): SeminarSearchResponse

    fun createSeminar(request: SeminarDto, mainImage: MultipartFile?, attachments: List<MultipartFile>?): SeminarDto
    fun readSeminar(seminarId: Long): SeminarDto
    fun updateSeminar(
        seminarId: Long,
        request: SeminarDto,
        newMainImage: MultipartFile?,
        newAttachments: List<MultipartFile>?
    ): SeminarDto

    fun deleteSeminar(seminarId: Long)
    fun getAllIds(): List<Long>
}

@Service
class SeminarServiceImpl(
    private val seminarRepository: SeminarRepository,
    private val mainImageService: MainImageService,
    private val attachmentService: AttachmentService
) : SeminarService {
    @Transactional(readOnly = true)
    override fun searchSeminar(
        keyword: String?,
        pageable: Pageable,
        usePageBtn: Boolean,
        sortBy: ContentSearchSortType
    ): SeminarSearchResponse {
        return seminarRepository.searchSeminar(keyword, pageable, usePageBtn, sortBy, isCurrentUserStaff())
    }

    @Transactional
    override fun createSeminar(
        request: SeminarDto,
        mainImage: MultipartFile?,
        attachments: List<MultipartFile>?
    ): SeminarDto {
        val newSeminar = SeminarEntity.of(request)

        if (mainImage != null) {
            mainImageService.uploadMainImage(newSeminar, mainImage)
        }

        if (attachments != null) {
            attachmentService.uploadAllAttachments(newSeminar, attachments)
        }

        seminarRepository.save(newSeminar)

        val imageURL = mainImageService.createImageURL(newSeminar.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(newSeminar.attachments)
        return SeminarDto.of(newSeminar, imageURL, attachmentResponses)
    }

    @Transactional(readOnly = true)
    override fun readSeminar(seminarId: Long): SeminarDto {
        val seminar: SeminarEntity = seminarRepository.findByIdOrNull(seminarId)
            ?: throw CserealException.Csereal404("존재하지 않는 세미나입니다.(seminarId: $seminarId)")

        if (seminar.isPrivate && !isCurrentUserStaff()) throw CserealException.Csereal401("접근 권한이 없습니다.")

        val imageURL = mainImageService.createImageURL(seminar.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(seminar.attachments)

        val prevSeminar =
            seminarRepository.findFirstByIsPrivateFalseAndCreatedAtLessThanOrderByCreatedAtDesc(
                seminar.createdAt!!
            )
        val nextSeminar =
            seminarRepository.findFirstByIsPrivateFalseAndCreatedAtGreaterThanOrderByCreatedAtAsc(
                seminar.createdAt!!
            )

        return SeminarDto.of(seminar, imageURL, attachmentResponses, prevSeminar, nextSeminar)
    }

    @Transactional
    override fun updateSeminar(
        seminarId: Long,
        request: SeminarDto,
        newMainImage: MultipartFile?,
        newAttachments: List<MultipartFile>?
    ): SeminarDto {
        val seminar: SeminarEntity = seminarRepository.findByIdOrNull(seminarId)
            ?: throw CserealException.Csereal404("존재하지 않는 세미나입니다")

        seminar.update(request)

        if (newMainImage != null) {
            seminar.mainImage?.isDeleted = true
            mainImageService.uploadMainImage(seminar, newMainImage)
        }

        attachmentService.deleteAttachmentsDeprecated(request.deleteIds)

        if (newAttachments != null) {
            attachmentService.uploadAllAttachments(seminar, newAttachments)
        }

        val attachmentResponses = attachmentService.createAttachmentResponses(seminar.attachments)

        val imageURL = mainImageService.createImageURL(seminar.mainImage)
        return SeminarDto.of(seminar, imageURL, attachmentResponses)
    }

    @Transactional
    override fun deleteSeminar(seminarId: Long) {
        seminarRepository.findByIdOrNull(seminarId)
            ?: throw CserealException.Csereal404("존재하지 않는 세미나입니다.(seminarId=$seminarId")

        seminarRepository.deleteById(seminarId)
    }

    @Transactional(readOnly = true)
    override fun getAllIds(): List<Long> {
        return seminarRepository.findAllIds()
    }
}
