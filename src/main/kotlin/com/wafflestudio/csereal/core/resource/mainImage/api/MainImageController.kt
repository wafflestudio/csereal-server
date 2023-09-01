package com.wafflestudio.csereal.core.resource.mainImage.api

import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.web.bind.annotation.*

@RequestMapping("/image")
@RestController
class MainImageController(
    private val mainImageService: MainImageService
) {

}