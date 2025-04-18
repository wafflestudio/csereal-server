package com.wafflestudio.csereal.core.resource.mainImage.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.controller.MainImageContentEntityType
import com.wafflestudio.csereal.common.properties.EndpointProperties
import com.wafflestudio.csereal.core.about.database.AboutEntity
import com.wafflestudio.csereal.core.council.database.CouncilEntity
import com.wafflestudio.csereal.core.member.database.ProfessorEntity
import com.wafflestudio.csereal.core.member.database.StaffEntity
import com.wafflestudio.csereal.core.news.database.NewsEntity
import com.wafflestudio.csereal.core.recruit.database.RecruitEntity
import com.wafflestudio.csereal.core.research.database.ResearchEntity
import com.wafflestudio.csereal.core.resource.common.event.FileDeleteEvent
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageRepository
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import com.wafflestudio.csereal.core.resource.mainImage.dto.MainImageDto
import com.wafflestudio.csereal.core.seminar.database.SeminarEntity
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.apache.commons.io.FilenameUtils
import org.springframework.context.ApplicationEventPublisher
import java.lang.invoke.WrongMethodTypeException
import java.nio.file.Files
import java.nio.file.Paths

interface MainImageService {
    fun uploadMainImage(
        contentEntityType: MainImageContentEntityType,
        requestImage: MultipartFile
    ): MainImageDto

    fun createImageURL(image: MainImageEntity?): String?

    fun removeImage(image: MainImageEntity)
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
        contentEntityType: MainImageContentEntityType,
        requestImage: MultipartFile
    ): MainImageDto {
        Files.createDirectories(Paths.get(path))

        val extension = FilenameUtils.getExtension(requestImage.originalFilename)

        if (!listOf("jpg", "jpeg", "png").contains(extension)) {
            throw CserealException.Csereal400("파일의 형식은 jpg, jpeg, png 중 하나여야 합니다.")
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

        connectMainImageToEntity(contentEntityType, mainImage)
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
    override fun removeImage(image: MainImageEntity) {
        val fileDirectory = path + image.filename
        mainImageRepository.delete(image)
        eventPublisher.publishEvent(FileDeleteEvent(fileDirectory))
    }

    // TODO: 각 entity의 interface로 refactoring하기.
    private fun connectMainImageToEntity(contentEntity: MainImageContentEntityType, mainImage: MainImageEntity) {
        when (contentEntity) {
            is NewsEntity -> {
                contentEntity.mainImage = mainImage
            }

            is SeminarEntity -> {
                contentEntity.mainImage = mainImage
            }

            is AboutEntity -> {
                contentEntity.mainImage = mainImage
            }

            is ProfessorEntity -> {
                contentEntity.mainImage = mainImage
            }

            is StaffEntity -> {
                contentEntity.mainImage = mainImage
            }

            is ResearchEntity -> {
                contentEntity.mainImage = mainImage
            }

            is RecruitEntity -> {
                contentEntity.mainImage = mainImage
            }

            is CouncilEntity -> {
                contentEntity.mainImage = mainImage
            }

            else -> {
                throw WrongMethodTypeException("해당하는 엔티티가 없습니다")
            }
        }
    }
}
