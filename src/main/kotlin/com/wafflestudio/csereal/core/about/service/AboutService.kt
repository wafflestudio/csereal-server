package com.wafflestudio.csereal.core.about.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.about.database.AboutEntity
import com.wafflestudio.csereal.core.about.database.AboutPostType
import com.wafflestudio.csereal.core.about.database.AboutRepository
import com.wafflestudio.csereal.core.about.database.LocationEntity
import com.wafflestudio.csereal.core.about.dto.AboutDto
import com.wafflestudio.csereal.core.resource.image.database.ImageEntity
import com.wafflestudio.csereal.core.resource.image.database.ImageRepository
import com.wafflestudio.csereal.core.resource.image.service.ImageService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface AboutService {
    fun createAbout(postType: String, request: AboutDto, image: MultipartFile?): AboutDto
    fun readAbout(postType: String): AboutDto
    fun readAllClubs() : List<AboutDto>
    fun readAllFacilities() : List<AboutDto>
    fun readAllDirections(): List<AboutDto>
}

@Service
class AboutServiceImpl(
    private val aboutRepository: AboutRepository,
    private val imageService: ImageService,
    private val imageRepository: ImageRepository,
) : AboutService {
    @Transactional
    override fun createAbout(postType: String, request: AboutDto, image: MultipartFile?): AboutDto {
        var imageEntity : ImageEntity? = null
        if(image != null) {
            val imageDto = imageService.uploadImage(image)
            imageEntity = imageRepository.findByFilenameAndExtension(imageDto.filename, imageDto.extension)
        }

        val enumPostType = makeStringToEnum(postType)
        val newAbout = AboutEntity.of(enumPostType, request, imageEntity)

        if(request.locations != null) {
            for (location in request.locations) {
                LocationEntity.create(location, newAbout)
            }
        }

        aboutRepository.save(newAbout)

        return AboutDto.of(newAbout)
    }

    @Transactional(readOnly = true)
    override fun readAbout(postType: String): AboutDto {
        val enumPostType = makeStringToEnum(postType)
        val about = aboutRepository.findByPostType(enumPostType)

        return AboutDto.of(about)
    }

    @Transactional(readOnly = true)
    override fun readAllClubs(): List<AboutDto> {
        val clubs = aboutRepository.findAllByPostTypeOrderByName(AboutPostType.STUDENT_CLUBS).map {
            AboutDto.of(it)
        }

        return clubs
    }

    @Transactional(readOnly = true)
    override fun readAllFacilities(): List<AboutDto> {
        val facilities = aboutRepository.findAllByPostTypeOrderByName(AboutPostType.FACILITIES).map {
            AboutDto.of(it)
        }

        return facilities
    }

    @Transactional(readOnly = true)
    override fun readAllDirections(): List<AboutDto> {
        val directions = aboutRepository.findAllByPostTypeOrderByName(AboutPostType.DIRECTIONS).map {
            AboutDto.of(it)
        }

        return directions
    }

    private fun makeStringToEnum(postType: String) : AboutPostType {
        try {
            val upperPostType = postType.replace("-","_").uppercase()
            return AboutPostType.valueOf(upperPostType)

        } catch (e: IllegalArgumentException) {
            throw CserealException.Csereal400("해당하는 enum을 찾을 수 없습니다")
        }
    }
}