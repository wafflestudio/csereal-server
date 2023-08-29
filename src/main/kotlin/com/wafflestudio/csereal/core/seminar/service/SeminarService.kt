package com.wafflestudio.csereal.core.seminar.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.resource.image.database.ImageEntity
import com.wafflestudio.csereal.core.resource.image.database.ImageRepository
import com.wafflestudio.csereal.core.resource.image.service.ImageService
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
    fun createSeminar(request: SeminarDto, image: MultipartFile?): SeminarDto
    fun readSeminar(seminarId: Long, keyword: String?): SeminarDto
    fun updateSeminar(seminarId: Long, request: SeminarDto): SeminarDto
    fun deleteSeminar(seminarId: Long)
}

@Service
class SeminarServiceImpl(
    private val seminarRepository: SeminarRepository,
    private val imageService: ImageService,
) : SeminarService {
    @Transactional(readOnly = true)
    override fun searchSeminar(keyword: String?, pageNum: Long): SeminarSearchResponse {
        return seminarRepository.searchSeminar(keyword, pageNum)
    }

    @Transactional
    override fun createSeminar(request: SeminarDto, image: MultipartFile?): SeminarDto {
        val newSeminar = SeminarEntity.of(request)

        if(image != null) {
            imageService.uploadImage(newSeminar, image)
        }

        seminarRepository.save(newSeminar)

        val imageURL = imageService.createImageURL(newSeminar.mainImage)


        return SeminarDto.of(newSeminar, imageURL, null)
    }

    @Transactional(readOnly = true)
    override fun readSeminar(seminarId: Long, keyword: String?): SeminarDto {
        val seminar: SeminarEntity = seminarRepository.findByIdOrNull(seminarId)
            ?: throw CserealException.Csereal404("존재하지 않는 세미나입니다.(seminarId: $seminarId)")

        if (seminar.isDeleted) throw CserealException.Csereal400("삭제된 세미나입니다. (seminarId: $seminarId)")

        val imageURL = imageService.createImageURL(seminar.mainImage)

        val prevNext = seminarRepository.findPrevNextId(seminarId, keyword)

        return SeminarDto.of(seminar, imageURL, prevNext)
    }

    @Transactional
    override fun updateSeminar(seminarId: Long, request: SeminarDto): SeminarDto {
        val seminar: SeminarEntity = seminarRepository.findByIdOrNull(seminarId)
            ?: throw CserealException.Csereal404("존재하지 않는 세미나입니다")
        if(seminar.isDeleted) throw CserealException.Csereal404("삭제된 세미나입니다. (seminarId: $seminarId)")

        seminar.update(request)

        val imageURL = imageService.createImageURL(seminar.mainImage)

        return SeminarDto.of(seminar, imageURL, null)
    }
    @Transactional
    override fun deleteSeminar(seminarId: Long) {
        val seminar: SeminarEntity = seminarRepository.findByIdOrNull(seminarId)
            ?: throw CserealException.Csereal404("존재하지 않는 세미나입니다.(seminarId=$seminarId")

        seminar.isDeleted = true
    }
}