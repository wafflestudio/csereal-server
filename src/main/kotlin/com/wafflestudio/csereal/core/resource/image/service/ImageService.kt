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
        setUUIDFilename: Boolean = true
    ): ImageDto
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

        if(!listOf("jpg", "jpeg", "png", "gif").contains(extension)) {
            throw CserealException.Csereal400("파일의 형식은 jpg, jpeg, png, gif 중 하나여야 합니다.")
        }

        val timeMillis = System.currentTimeMillis()
        val uuid : String? = if (setUUIDFilename) {
            UUID.randomUUID().toString()
        } else {
            requestImage.originalFilename
        }

        val filename = "${timeMillis}_$uuid"
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
            size = requestImage.size
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

    fun connectImageToEntity(contentEntity: ContentEntityType, image: ImageEntity) {
        when (contentEntity) {
            is NewsEntity -> {
                contentEntity.mainImage = image
                image.news = contentEntity
            }
            is SeminarEntity -> {
                contentEntity.mainImage = image
                image.seminar = contentEntity
            }
            is AboutEntity -> {
                contentEntity.mainImage = image
                image.about = contentEntity
            }
            is ProfessorEntity -> {
                contentEntity.mainImage = image
                image.professor = contentEntity
            }
            is StaffEntity -> {
                contentEntity.mainImage = image
                image.staff = contentEntity
            }
            else -> {
                throw WrongMethodTypeException("해당하는 엔티티가 없습니다")
            }
        }


    }

}