package com.wafflestudio.csereal.core.seminar.api

import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import com.wafflestudio.csereal.core.seminar.dto.SeminarDto
import com.wafflestudio.csereal.core.seminar.dto.SeminarSearchResponse
import com.wafflestudio.csereal.core.seminar.service.SeminarService
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v1/seminar")
@RestController
class SeminarController(
    private val seminarService: SeminarService,
) {
    @GetMapping
    fun searchSeminar(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false, defaultValue = "1") pageNum: Int
    ): ResponseEntity<SeminarSearchResponse> {
        val pageSize = 10
        val pageRequest = PageRequest.of(pageNum - 1, pageSize)
        val usePageBtn = pageNum != 1
        return ResponseEntity.ok(seminarService.searchSeminar(keyword, pageRequest, usePageBtn))
    }

    @PostMapping
    fun createSeminar(
        @Valid @RequestPart("request") request: SeminarDto,
        @RequestPart("mainImage") mainImage: MultipartFile?,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ): ResponseEntity<SeminarDto> {
        return ResponseEntity.ok(seminarService.createSeminar(request, mainImage, attachments))
    }

    @GetMapping("/{seminarId}")
    fun readSeminar(
        @PathVariable seminarId: Long
    ): ResponseEntity<SeminarDto> {
        return ResponseEntity.ok(seminarService.readSeminar(seminarId))
    }

    @PatchMapping("/{seminarId}")
    fun updateSeminar(
        @PathVariable seminarId: Long,
        @Valid @RequestPart("request") request: SeminarDto,
        @RequestPart("newMainImage") newMainImage: MultipartFile?,
        @RequestPart("newAttachments") newAttachments: List<MultipartFile>?,
        @RequestPart("attachmentsList") attachmentsList: List<AttachmentResponse>,
    ): ResponseEntity<SeminarDto> {
        return ResponseEntity.ok(
            seminarService.updateSeminar(
                seminarId,
                request,
                newMainImage,
                newAttachments,
                attachmentsList
            )
        )
    }

    @DeleteMapping("/{seminarId}")
    fun deleteSeminar(
        @PathVariable seminarId: Long
    ) {
        seminarService.deleteSeminar(seminarId)
    }
}
