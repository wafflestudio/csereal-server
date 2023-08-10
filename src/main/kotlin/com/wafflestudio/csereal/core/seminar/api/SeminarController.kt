package com.wafflestudio.csereal.core.seminar.api

import com.wafflestudio.csereal.core.seminar.dto.SeminarDto
import com.wafflestudio.csereal.core.seminar.service.SeminarService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/seminar")
@RestController
class SeminarController (
    private val seminarService: SeminarService,
) {
    @PostMapping
    fun createSeminar(
        @Valid @RequestBody request: SeminarDto
    ) : ResponseEntity<SeminarDto> {
        return ResponseEntity.ok(seminarService.createSeminar(request))
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
        @Valid @RequestBody request: SeminarDto,
    ) : ResponseEntity<SeminarDto> {
        return ResponseEntity.ok(seminarService.updateSeminar(seminarId, request))
    }
}