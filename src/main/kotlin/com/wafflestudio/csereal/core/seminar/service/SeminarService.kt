package com.wafflestudio.csereal.core.seminar.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import com.wafflestudio.csereal.core.resource.mainImage.service.ImageService
import com.wafflestudio.csereal.core.seminar.database.SeminarEntity
import com.wafflestudio.csereal.core.seminar.database.SeminarRepository
import com.wafflestudio.csereal.core.seminar.dto.SeminarDto
import com.wafflestudio.csereal.core.seminar.dto.SeminarSearchResponse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface SeminarService {
    fun searchSeminar(keyword: String?, pageNum: Long): SeminarSearchResponse
    fun createSeminar(request: SeminarDto, image: MultipartFile?, attachments: List<MultipartFile>?): SeminarDto
    fun readSeminar(seminarId: Long, keyword: String?): SeminarDto
    fun updateSeminar(seminarId: Long, request: SeminarDto): SeminarDto
    fun deleteSeminar(seminarId: Long)
}

@Service
class SeminarServiceImpl(
    private val seminarRepository: SeminarRepository,
    private val imageService: ImageService,
    private val attachmentService: AttachmentService,
) : SeminarService {
    @Transactional(readOnly = true)
    override fun searchSeminar(keyword: String?, pageNum: Long): SeminarSearchResponse {
        return seminarRepository.searchSeminar(keyword, pageNum)
    }

    @Transactional
    override fun createSeminar(request: SeminarDto, image: MultipartFile?, attachments: List<MultipartFile>?): SeminarDto {
        val newSeminar = SeminarEntity.of(request)

        if(image != null) {
            imageService.uploadImage(newSeminar, image)
        }

        if(attachments != null) {
            attachmentService.uploadAttachments(newSeminar, attachments)
        }

        seminarRepository.save(newSeminar)

        val imageURL = imageService.createImageURL(newSeminar.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(newSeminar.attachments)

        return SeminarDto.of(newSeminar, imageURL, attachmentResponses, null)
    }

    @Transactional(readOnly = true)
    override fun readSeminar(seminarId: Long, keyword: String?): SeminarDto {
        val seminar: SeminarEntity = seminarRepository.findByIdOrNull(seminarId)
            ?: throw CserealException.Csereal404("존재하지 않는 세미나입니다.(seminarId: $seminarId)")

        if (seminar.isDeleted) throw CserealException.Csereal400("삭제된 세미나입니다. (seminarId: $seminarId)")

        val imageURL = imageService.createImageURL(seminar.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(seminar.attachments)

        val prevNext = seminarRepository.findPrevNextId(seminarId, keyword)

        return SeminarDto.of(seminar, imageURL, attachmentResponses, prevNext)
    }

    @Transactional
    override fun updateSeminar(seminarId: Long, request: SeminarDto): SeminarDto {
        val seminar: SeminarEntity = seminarRepository.findByIdOrNull(seminarId)
            ?: throw CserealException.Csereal404("존재하지 않는 세미나입니다")
        if(seminar.isDeleted) throw CserealException.Csereal404("삭제된 세미나입니다. (seminarId: $seminarId)")

        seminar.update(request)

        val imageURL = imageService.createImageURL(seminar.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(seminar.attachments)


        return SeminarDto.of(seminar, imageURL, attachmentResponses, null)
    }
    @Transactional
    override fun deleteSeminar(seminarId: Long) {
        val seminar: SeminarEntity = seminarRepository.findByIdOrNull(seminarId)
            ?: throw CserealException.Csereal404("존재하지 않는 세미나입니다.(seminarId=$seminarId")

        seminar.isDeleted = true
    }
}