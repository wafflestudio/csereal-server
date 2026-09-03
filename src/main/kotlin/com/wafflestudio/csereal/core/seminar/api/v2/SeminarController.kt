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
        @RequestParam(required = false, defaultValue = "DATE") sortBy: ContentSearchSortType
    ): ResponseEntity<SeminarSearchResponse> {
        val usePageBtn = pageNum != null
        val page = pageNum ?: 1
        val pageRequest = PageRequest.of(page - 1, pageSize)

        return ResponseEntity.ok(seminarService.searchSeminar(keyword, pageRequest, usePageBtn, sortBy))
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping(consumes = ["multipart/form-data"])
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
    @PatchMapping("/{seminarId}", consumes = ["multipart/form-data"])
    fun updateSeminar(
        @PathVariable seminarId: Long,
        @Valid
        @RequestPart("request")
        request: SeminarDto,
        @RequestPart("newMainImage") newMainImage: MultipartFile?,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ): ResponseEntity<SeminarDto> {
        return ResponseEntity.ok(
            seminarService.updateSeminar(
                seminarId,
                request,
                newMainImage,
                attachments
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
