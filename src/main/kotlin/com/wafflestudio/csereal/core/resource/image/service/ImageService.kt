package com.wafflestudio.csereal.core.resource.image.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.controller.ContentEntityType
import com.wafflestudio.csereal.core.about.database.AboutEntity
import com.wafflestudio.csereal.core.member.database.ProfessorEntity
import com.wafflestudio.csereal.core.member.database.StaffEntity
import com.wafflestudio.csereal.core.news.database.NewsEntity
import com.wafflestudio.csereal.core.resource.image.database.ImageEntity
import com.wafflestudio.csereal.core.resource.image.database.ImageRepository
import com.wafflestudio.csereal.core.resource.image.dto.ImageDto
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
import java.util.*
import kotlin.io.path.fileSize
import kotlin.io.path.name


interface ImageService {
    fun uploadImage(
        contentEntityType: ContentEntityType,
        requestImage: MultipartFile,
        setUUIDFilename: Boolean = false
    ): ImageDto
    fun createImageURL(image: ImageEntity?) : String?
}

@Service
class ImageServiceImpl(
    private val imageRepository: ImageRepository,
    @Value("\${csereal.upload.path}")
    private val path: String,
) : ImageService {

    @Transactional
    override fun uploadImage(
        contentEntity: ContentEntityType,
        requestImage: MultipartFile,
        setUUIDFilename: Boolean,
    ): ImageDto {
        Files.createDirectories(Paths.get(path))

        val extension = FilenameUtils.getExtension(requestImage.originalFilename)

        if(!listOf("jpg", "jpeg", "png").contains(extension)) {
            throw CserealException.Csereal400("파일의 형식은 jpg, jpeg, png, gif 중 하나여야 합니다.")
        }

        val timeMillis = System.currentTimeMillis()
        val originalFilename : String? = if (setUUIDFilename) {
            UUID.randomUUID().toString()
        } else {
            requestImage.name
        }

        val filename = "${originalFilename}_$timeMillis"
        val totalFilename = path + filename
        val saveFile = Paths.get("$totalFilename.$extension")
        requestImage.transferTo(saveFile)

        val totalThumbnailFilename = "${path}thumbnail_$filename"
        val thumbnailFile = Paths.get("$totalThumbnailFilename.$extension")
        Thumbnailator.createThumbnail(saveFile.toFile(), thumbnailFile.toFile(), 100, 100);

        val image = ImageEntity(
            filename = filename,
            extension = extension,
            imagesOrder = 1,
            size = requestImage.size,
        )

        val thumbnail = ImageEntity(
            filename = thumbnailFile.name,
            extension = extension,
            imagesOrder = 1,
            size = thumbnailFile.fileSize()
        )

        connectImageToEntity(contentEntity, image)
        imageRepository.save(image)
        imageRepository.save(thumbnail)

        return ImageDto(
            filename = filename,
            extension = extension,
            imagesOrder = 1,
            size = requestImage.size
        )
    }

    @Transactional
    override fun createImageURL(image: ImageEntity?) : String? {
        return if(image != null) {
            "http://cse-dev-waffle.bacchus.io/var/myapp/image/${image.filename}.${image.extension}"
        } else null
    }

    private fun connectImageToEntity(contentEntity: ContentEntityType, image: ImageEntity) {
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