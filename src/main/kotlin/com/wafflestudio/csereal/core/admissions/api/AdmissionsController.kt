package com.wafflestudio.csereal.core.admissions.api

import com.wafflestudio.csereal.core.admissions.database.AdmissionPostType
import com.wafflestudio.csereal.core.admissions.database.StudentType
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

@RequestMapping("/admissions")
@RestController
class AdmissionsController(
    private val admissionsService: AdmissionsService
) {
    @PostMapping
    fun createAdmissions(
        @RequestParam studentType: StudentType,
        @Valid @RequestBody request: AdmissionsDto
    ) : AdmissionsDto {
        return admissionsService.createAdmissions(studentType, request)
    }

    @GetMapping
    fun readAdmissionsMain(
        @RequestParam studentType: StudentType,
    ) : ResponseEntity<AdmissionsDto> {
        return ResponseEntity.ok(admissionsService.readAdmissionsMain(studentType))
    }
    @GetMapping("/undergraduate")
    fun readUndergraduateAdmissions(
        @RequestParam postType: AdmissionPostType
    ) : ResponseEntity<AdmissionsDto> {
        return ResponseEntity.ok(admissionsService.readUndergraduateAdmissions(postType))
    }


}