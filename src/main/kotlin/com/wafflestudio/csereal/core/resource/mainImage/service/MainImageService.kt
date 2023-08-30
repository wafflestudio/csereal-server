package com.wafflestudio.csereal.core.resource.mainImage.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.controller.ImageContentEntityType
import com.wafflestudio.csereal.common.properties.EndpointProperties
import com.wafflestudio.csereal.core.about.database.AboutEntity
import com.wafflestudio.csereal.core.member.database.ProfessorEntity
import com.wafflestudio.csereal.core.member.database.StaffEntity
import com.wafflestudio.csereal.core.news.database.NewsEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageRepository
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import com.wafflestudio.csereal.core.resource.mainImage.dto.MainImageDto
import com.wafflestudio.csereal.core.seminar.database.SeminarEntity
import net.coobird.thumbnailator.Thumbnailator
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.apache.commons.io.FilenameUtils
import java.lang.invoke.WrongMethodTypeException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.fileSize
import kotlin.io.path.name


interface ImageService {
    fun uploadImage(
        contentEntityType: ImageContentEntityType,
        requestImage: MultipartFile,
    ): MainImageDto

    fun createImageURL(image: MainImageEntity?): String?
}

@Service
class ImageServiceImpl(
    private val imageRepository: MainImageRepository,
    @Value("\${csereal_image.upload.path}")
    private val path: String,
    private val endpointProperties: EndpointProperties
) : ImageService {

    @Transactional
    override fun uploadImage(
        contentEntity: ImageContentEntityType,
        requestImage: MultipartFile,
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

        val totalThumbnailFilename = "${path}thumbnail_$filename"
        val thumbnailFile = Paths.get("$totalThumbnailFilename.$extension")
        Thumbnailator.createThumbnail(saveFile.toFile(), thumbnailFile.toFile(), 100, 100);

        val image = MainImageEntity(
            filename = filename,
            imagesOrder = 1,
            size = requestImage.size,
        )

        val thumbnail = MainImageEntity(
            filename = thumbnailFile.name,
            imagesOrder = 1,
            size = thumbnailFile.fileSize()
        )

        connectImageToEntity(contentEntity, image)
        imageRepository.save(image)
        imageRepository.save(thumbnail)

        return MainImageDto(
            filename = filename,
            imagesOrder = 1,
            size = requestImage.size
        )
    }

    @Transactional
    override fun createImageURL(image: MainImageEntity?): String? {
        return if (image != null) {
            "${endpointProperties.backend}/image/${image.filename}"
        } else null
    }

    private fun connectImageToEntity(contentEntity: ImageContentEntityType, image: MainImageEntity) {
        when (contentEntity) {
            is NewsEntity -> {
                contentEntity.mainImage = image
            }

            is SeminarEntity -> {
                contentEntity.mainImage = image
            }

            is AboutEntity -> {
                contentEntity.mainImage = image
            }

            is ProfessorEntity -> {
                contentEntity.mainImage = image
            }

            is StaffEntity -> {
                contentEntity.mainImage = image
            }

            else -> {
                throw WrongMethodTypeException("해당하는 엔티티가 없습니다")
            }
        }
    }

}
