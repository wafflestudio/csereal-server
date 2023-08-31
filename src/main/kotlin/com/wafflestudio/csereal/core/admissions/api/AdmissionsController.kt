package com.wafflestudio.csereal.core.admissions.api

import com.wafflestudio.csereal.core.admissions.dto.AdmissionsDto
import com.wafflestudio.csereal.core.admissions.service.AdmissionsService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/admissions")
@RestController
class AdmissionsController(
    private val admissionsService: AdmissionsService
) {
    @PostMapping("/undergraduate")
    fun createUndergraduateAdmissions(
        @RequestParam postType: String,
        @Valid @RequestBody request: AdmissionsDto
    ) : AdmissionsDto {
        return admissionsService.createUndergraduateAdmissions(postType, request)
    }

    @PostMapping("/graduate")
    fun createGraduateAdmissions(
        @Valid @RequestBody request: AdmissionsDto
    ) : AdmissionsDto {
        return admissionsService.createGraduateAdmissions(request)
    }

    @GetMapping("/undergraduate")
    fun readUndergraduateAdmissions(
        @RequestParam postType: String
    ) : ResponseEntity<AdmissionsDto> {
        return ResponseEntity.ok(admissionsService.readUndergraduateAdmissions(postType))
    }

    @GetMapping("/graduate")
    fun readGraduateAdmissions() : ResponseEntity<AdmissionsDto> {
        return ResponseEntity.ok(admissionsService.readGraduateAdmissions())
    }




}