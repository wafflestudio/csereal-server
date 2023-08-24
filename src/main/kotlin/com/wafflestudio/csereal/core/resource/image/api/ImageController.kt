package com.wafflestudio.csereal.core.resource.image.api

import com.wafflestudio.csereal.core.resource.image.dto.ImageDto
import com.wafflestudio.csereal.core.resource.image.service.ImageService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


@RequestMapping("/image")
@RestController
class ImageController(
    private val imageService: ImageService
) {
    @PostMapping(consumes = ["multipart/form-data"])
    fun uploadImage(@RequestParam requestImage: MultipartFile) : ImageDto {
        return imageService.uploadImage(requestImage)
    }

}