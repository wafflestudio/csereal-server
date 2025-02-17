package com.wafflestudio.csereal.core.seminar.api.v2

import com.wafflestudio.csereal.common.enums.ContentSearchSortType
import com.wafflestudio.csereal.core.seminar.dto.SeminarDto
import com.wafflestudio.csereal.core.seminar.dto.SeminarSearchResponse
import com.wafflestudio.csereal.core.seminar.service.SeminarService
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v2/seminar")
@RestController
class SeminarController(
    private val seminarService: SeminarService
) {
    @GetMapping
    fun searchSeminar(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) pageNum: Int?,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int,
        @RequestParam(required = false, defaultValue = "DATE") sortBy: String
    ): ResponseEntity<SeminarSearchResponse> {
        val usePageBtn = pageNum != null
        val page = pageNum ?: 1
        val pageRequest = PageRequest.of(page - 1, pageSize)

        val sortType = ContentSearchSortType.fromJsonValue(sortBy)

        return ResponseEntity.ok(seminarService.searchSeminar(keyword, pageRequest, usePageBtn, sortType))
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping
    fun createSeminar(
        @Valid
        @RequestPart("request")
        request: SeminarDto,
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

    @PreAuthorize("hasRole('STAFF')")
    @PatchMapping("/{seminarId}")
    fun updateSeminar(
        @PathVariable seminarId: Long,
        @Valid
        @RequestPart("request")
        request: SeminarDto,
        @RequestPart("newMainImage") newMainImage: MultipartFile?,
        @RequestPart("newAttachments") newAttachments: List<MultipartFile>?
    ): ResponseEntity<SeminarDto> {
        return ResponseEntity.ok(
            seminarService.updateSeminar(
                seminarId,
                request,
                newMainImage,
                newAttachments
            )
        )
    }

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/{seminarId}")
    fun deleteSeminar(
        @PathVariable seminarId: Long
    ) {
        seminarService.deleteSeminar(seminarId)
    }

    @GetMapping("/ids")
    fun getAllIds(): ResponseEntity<List<Long>> {
        return ResponseEntity.ok(seminarService.getAllIds())
    }
}
