package com.wafflestudio.csereal.core.resource.image.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.resource.image.database.ImageEntity
import com.wafflestudio.csereal.core.resource.image.database.ImageRepository
import com.wafflestudio.csereal.core.resource.image.dto.ImageDto
import net.coobird.thumbnailator.Thumbnailator
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.apache.commons.io.FilenameUtils
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.fileSize
import kotlin.io.path.name


interface ImageService {
    fun uploadImage(
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

        imageRepository.save(image)
        imageRepository.save(thumbnail)

        return ImageDto(
            filename = filename,
            extension = extension,
            imagesOrder = 1,
            size = requestImage.size
        )
    }

}