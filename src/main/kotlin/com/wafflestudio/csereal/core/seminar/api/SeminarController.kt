package com.wafflestudio.csereal.core.seminar.api

import com.wafflestudio.csereal.core.seminar.dto.SeminarDto
import com.wafflestudio.csereal.core.seminar.dto.SeminarSearchResponse
import com.wafflestudio.csereal.core.seminar.service.SeminarService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v1/seminar")
@RestController
class SeminarController (
    private val seminarService: SeminarService,
) {
    @GetMapping
    fun searchSeminar(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false, defaultValue = "0") pageNum: Long
    ) : ResponseEntity<SeminarSearchResponse> {
        return ResponseEntity.ok(seminarService.searchSeminar(keyword, pageNum))
    }
    @PostMapping
    fun createSeminar(
        @Valid @RequestPart("request") request: SeminarDto,
        @RequestPart("mainImage") mainImage: MultipartFile?,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ) : ResponseEntity<SeminarDto> {
        return ResponseEntity.ok(seminarService.createSeminar(request, mainImage, attachments))
    }

    @GetMapping("/{seminarId}")
    fun readSeminar(
        @PathVariable seminarId: Long,
        @RequestParam(required = false) keyword: String?,
    ) : ResponseEntity<SeminarDto> {
        return ResponseEntity.ok(seminarService.readSeminar(seminarId, keyword))
    }

    @PatchMapping("/{seminarId}")
    fun updateSeminar(
        @PathVariable seminarId: Long,
        @Valid @RequestPart("request") request: SeminarDto,
        @RequestPart("mainImage") mainImage: MultipartFile?,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ) : ResponseEntity<SeminarDto> {
        return ResponseEntity.ok(seminarService.updateSeminar(seminarId, request, mainImage, attachments))
    }

    @DeleteMapping("/{seminarId}")
    fun deleteSeminar(
        @PathVariable seminarId: Long
    ) {
        seminarService.deleteSeminar(seminarId)
    }
}