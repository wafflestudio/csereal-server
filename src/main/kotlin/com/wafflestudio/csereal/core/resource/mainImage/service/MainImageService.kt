package com.wafflestudio.csereal.core.resource.mainImage.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.entity.MainImageAttachable
import com.wafflestudio.csereal.common.properties.EndpointProperties
import com.wafflestudio.csereal.core.resource.common.event.FileDeleteEvent
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageRepository
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import com.wafflestudio.csereal.core.resource.mainImage.dto.MainImageDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.apache.commons.io.FilenameUtils
import org.springframework.context.ApplicationEventPublisher
import java.nio.file.Files
import java.nio.file.Paths

interface MainImageService {
    fun uploadMainImage(
        contentEntityType: MainImageAttachable,
        requestImage: MultipartFile
    ): MainImageDto

    fun createImageURL(image: MainImageEntity?): String?

    fun removeImage(image: MainImageEntity)

    fun replaceMainImage(
        owner: MainImageAttachable,
        newImage: MultipartFile?,
        removeImage: Boolean
    )
}

@Service
class MainImageServiceImpl(
    private val mainImageRepository: MainImageRepository,
    @Value("\${csereal.upload.path}")
    private val path: String,
    private val endpointProperties: EndpointProperties,
    private val eventPublisher: ApplicationEventPublisher
) : MainImageService {

    @Transactional
    override fun uploadMainImage(
        contentEntityType: MainImageAttachable,
        requestImage: MultipartFile
    ): MainImageDto {
        Files.createDirectories(Paths.get(path))

        val extension = FilenameUtils.getExtension(requestImage.originalFilename)?.lowercase()

        if (!listOf("jpg", "jpeg", "png").contains(extension)) {
            throw CserealException(ErrorCode.INVALID_IMAGE_TYPE)
        }

        val timeMillis = System.currentTimeMillis()

        val filename = "${timeMillis}_${requestImage.originalFilename}"
        val totalFilename = path + filename
        val saveFile = Paths.get(totalFilename)
        requestImage.transferTo(saveFile)

        val mainImage = MainImageEntity(
            filename = filename,
            imagesOrder = 1,
            size = requestImage.size
        )

        contentEntityType.mainImage = mainImage
        mainImageRepository.save(mainImage)

        return MainImageDto(
            filename = filename,
            imagesOrder = 1,
            size = requestImage.size
        )
    }

    // TODO: `MainImageEntity`의 메서드로 refactoring하기.
    @Transactional
    override fun createImageURL(mainImage: MainImageEntity?): String? {
        return if (mainImage != null) {
            "${endpointProperties.backend}/v1/file/${mainImage.filename}"
        } else {
            null
        }
    }

    @Transactional
    override fun replaceMainImage(
        owner: MainImageAttachable,
        newImage: MultipartFile?,
        removeImage: Boolean
    ) {
        // 먼저 떼고 나중에 올려도 안전하다 — 업로드가 실패하면 트랜잭션이 롤백되고,
        // 파일 삭제는 AFTER_COMMIT 리스너(CommonFileService)가 하므로 파일은 남는다.
        when {
            newImage != null -> {
                owner.mainImage?.let { removeImage(it) }
                uploadMainImage(owner, newImage)
            }

            removeImage -> {
                owner.mainImage?.let { removeImage(it) }
                owner.mainImage = null
            }
        }
    }

    @Transactional
    override fun removeImage(image: MainImageEntity) {
        val fileDirectory = path + image.filename
        mainImageRepository.delete(image)
        eventPublisher.publishEvent(FileDeleteEvent(fileDirectory))
    }
}
