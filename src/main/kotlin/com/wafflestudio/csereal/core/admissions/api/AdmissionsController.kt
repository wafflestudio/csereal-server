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
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/admissions")
@RestController
class AdmissionsController(
    private val admissionsService: AdmissionsService
) {
    // postType -> 메인: main (학부, 대학원 모두 o), 수시: early-admission, 정시: regular-admission (수시 정시는 학부만)
    @PostMapping
    fun createAdmissions(
        @Valid @RequestBody request: AdmissionsDto
    ) : AdmissionsDto {
        return admissionsService.createAdmissions(request)
    }

    /*
    @GetMapping("/{postType}")
    fun readAdmissionsUndergraduate(
        @PathVariable postType: String
    ) : ResponseEntity<List<AdmissionsDto>> {
        return ResponseEntity.ok(admissionsService.readAdmissionsUndergraduate(postType))
    }

     */

    @GetMapping("/{to}")
    fun readAdmissionsMain(
        @PathVariable to: String,
    ) : ResponseEntity<AdmissionsDto> {
        return ResponseEntity.ok(admissionsService.readAdmissionsMain(to))
    }
    @GetMapping("/undergraduate/{postType}")
    fun readUndergraduateAdmissions(
        @PathVariable postType: String
    ) : ResponseEntity<AdmissionsDto> {
        return ResponseEntity.ok(admissionsService.readUndergraduateAdmissions(postType))
    }


}