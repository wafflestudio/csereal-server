package com.wafflestudio.csereal.core.resource.attachment.api

import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/attachment")
@RestController
class AttachmentController(
    private val attachmentService: AttachmentService
) {

}