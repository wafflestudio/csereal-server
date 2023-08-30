package com.wafflestudio.csereal.core.about.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.about.database.AboutEntity
import com.wafflestudio.csereal.core.about.database.AboutPostType
import com.wafflestudio.csereal.core.about.database.AboutRepository
import com.wafflestudio.csereal.core.about.database.LocationEntity
import com.wafflestudio.csereal.core.about.dto.AboutDto
import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import com.wafflestudio.csereal.core.resource.mainImage.service.ImageService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface AboutService {
    fun createAbout(postType: String, request: AboutDto, image: MultipartFile?, attachments: List<MultipartFile>?): AboutDto
    fun readAbout(postType: String): AboutDto
    fun readAllClubs() : List<AboutDto>
    fun readAllFacilities() : List<AboutDto>
    fun readAllDirections(): List<AboutDto>
}

@Service
class AboutServiceImpl(
    private val aboutRepository: AboutRepository,
    private val imageService: ImageService,
    private val attachmentService: AttachmentService,
) : AboutService {
    @Transactional
    override fun createAbout(postType: String, request: AboutDto, image: MultipartFile?, attachments: List<MultipartFile>?): AboutDto {
        val enumPostType = makeStringToEnum(postType)
        val newAbout = AboutEntity.of(enumPostType, request)

        if(request.locations != null) {
            for (location in request.locations) {
                LocationEntity.create(location, newAbout)
            }
        }

        if(image != null) {
            imageService.uploadImage(newAbout, image)
        }

        if(attachments != null) {
            attachmentService.uploadAttachments(newAbout, attachments)
        }
        aboutRepository.save(newAbout)

        val imageURL = imageService.createImageURL(newAbout.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(newAbout.attachments)

        return AboutDto.of(newAbout, imageURL, attachmentResponses)
    }

    @Transactional(readOnly = true)
    override fun readAbout(postType: String): AboutDto {
        val enumPostType = makeStringToEnum(postType)
        val about = aboutRepository.findByPostType(enumPostType)
        val imageURL = imageService.createImageURL(about.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(about.attachments)


        return AboutDto.of(about, imageURL, attachmentResponses)
    }

    @Transactional(readOnly = true)
    override fun readAllClubs(): List<AboutDto> {
        val clubs = aboutRepository.findAllByPostTypeOrderByName(AboutPostType.STUDENT_CLUBS).map {
            val imageURL = imageService.createImageURL(it.mainImage)
            val attachmentResponses = attachmentService.createAttachmentResponses(it.attachments)
            AboutDto.of(it, imageURL, attachmentResponses)
        }

        return clubs
    }

    @Transactional(readOnly = true)
    override fun readAllFacilities(): List<AboutDto> {
        val facilities = aboutRepository.findAllByPostTypeOrderByName(AboutPostType.FACILITIES).map {
            val imageURL = imageService.createImageURL(it.mainImage)
            val attachments = attachmentService.createAttachmentResponses(it.attachments)
            AboutDto.of(it, imageURL, attachments)
        }

        return facilities
    }

    @Transactional(readOnly = true)
    override fun readAllDirections(): List<AboutDto> {
        val directions = aboutRepository.findAllByPostTypeOrderByName(AboutPostType.DIRECTIONS).map {
            val imageURL = imageService.createImageURL(it.mainImage)
            val attachments = attachmentService.createAttachmentResponses(it.attachments)
            AboutDto.of(it, imageURL, attachments)
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