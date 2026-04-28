package com.wafflestudio.csereal.core.imagemodal.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.imagemodal.api.req.CreateImageModalReq
import com.wafflestudio.csereal.core.imagemodal.database.ImageModalEntity
import com.wafflestudio.csereal.core.imagemodal.database.ImageModalRepository
import com.wafflestudio.csereal.core.imagemodal.dto.ImageModalDto
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface ImageModalService {
    fun createImageModal(request: CreateImageModalReq, image: MultipartFile): ImageModalDto
    fun updateImageModal(modalId: Long, request: CreateImageModalReq, newImage: MultipartFile?): ImageModalDto
    fun deleteImageModal(id: Long)
    fun readImageModals(): List<ImageModalDto>
}

@Service
class ImageModalServiceImpl(
    private val imageModalRepository: ImageModalRepository,
    private val mainImageService: MainImageService
) : ImageModalService {
    @Transactional
    override fun createImageModal(
        request: CreateImageModalReq,
        image: MultipartFile
    ): ImageModalDto {
        val newImageModal = ImageModalEntity.of(request)
        mainImageService.uploadMainImage(newImageModal, image)
        imageModalRepository.save(newImageModal)

        val imageUrl = mainImageService.createImageURL(newImageModal.mainImage)!!

        return ImageModalDto.of(newImageModal, imageUrl)
    }

    @Transactional
    override fun updateImageModal(
        modalId: Long,
        request: CreateImageModalReq,
        newImage: MultipartFile?
    ): ImageModalDto {
        val imageModal: ImageModalEntity = getImageModalByIdOrThrow(modalId)

        imageModal.update(request)

        if (newImage != null) {
            val originalImage = imageModal.mainImage
            mainImageService.uploadMainImage(imageModal, newImage)
            originalImage?.let { mainImageService.removeImage(it) }
        }
        val imageUrl = mainImageService.createImageURL(imageModal.mainImage)!!

        return ImageModalDto.of(imageModal, imageUrl)
    }

    @Transactional
    override fun deleteImageModal(id: Long) {
        val imageModal = getImageModalByIdOrThrow(id)

        imageModal.mainImage?.let {
            imageModal.mainImage = null
            mainImageService.removeImage(it)
        }

        imageModalRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    override fun readImageModals(): List<ImageModalDto> {
        val imageModals = imageModalRepository.findAll()
        return imageModals.map {
            ImageModalDto.of(
                it,
                mainImageService.createImageURL(it.mainImage)!!
            )
        }
    }

    private fun getImageModalByIdOrThrow(imageModalId: Long): ImageModalEntity {
        return imageModalRepository.findByIdOrNull(imageModalId) ?: throw CserealException.Csereal404(
            "ImageModal Not Found"
        )
    }
}
