package com.wafflestudio.csereal.core.resource.mainImage.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.entity.MainImageAttachable
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
import java.nio.file.Files
import java.nio.file.Paths

interface MainImageService {
    fun uploadMainImage(
        contentEntityType: MainImageAttachable,
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
        contentEntityType: MainImageAttachable,
        requestImage: MultipartFile
    ): MainImageDto {
        val directory = mainImageDirectoryOf(contentEntityType)
        val uploadDir = Paths.get(path, directory)
        Files.createDirectories(uploadDir)

        val extension = FilenameUtils.getExtension(requestImage.originalFilename)

        if (!listOf("jpg", "jpeg", "png").contains(extension)) {
            throw CserealException.Csereal400("파일의 형식은 jpg, jpeg, png 중 하나여야 합니다.")
        }

        val timeMillis = System.currentTimeMillis()

        val filename = "${timeMillis}_${requestImage.originalFilename}"
        val saveFile = uploadDir.resolve(filename)
        requestImage.transferTo(saveFile)

        val mainImage = MainImageEntity(
            filename = filename,
            directory = directory,
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
    override fun createImageURL(image: MainImageEntity?): String? {
        return if (image != null) {
            "${endpointProperties.backend}/v1/file/${image.filePath()}"
        } else {
            null
        }
    }

    @Transactional
    override fun removeImage(image: MainImageEntity) {
        val fileDirectory = path + image.filePath()
        mainImageRepository.delete(image)
        eventPublisher.publishEvent(FileDeleteEvent(fileDirectory))
    }

    private fun mainImageDirectoryOf(contentEntityType: MainImageAttachable): String {
        return "mainImage/" + when (contentEntityType) {
            is NewsEntity -> "news"
            is SeminarEntity -> "seminar"
            is AboutEntity -> "about"
            is ProfessorEntity -> "professor"
            is StaffEntity -> "staff"
            is ResearchEntity -> "research"
            is RecruitEntity -> "recruit"
            is CouncilEntity -> "council"
            else -> ""
        }
    }
}
