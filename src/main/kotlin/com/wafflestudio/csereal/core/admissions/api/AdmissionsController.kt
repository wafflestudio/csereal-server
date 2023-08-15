package com.wafflestudio.csereal.core.admissions.api

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/admission")
@RestController
class AdmissionsController(
    private val admissionsService: AdmissionsService
) {
    @PostMapping("/undergraduate/{admissionsType}")
    fun createAdmissionUndergraduate(
        @PathVariable admissionsType: String,
        @Valid @RequestBody request: AdmissionsDto
    ) : AdmissionsDto {
        return admissionsService.createAdmissionsUndergraduate(admissionsType, request)
    }

    @GetMapping("/undergraduate")
    fun readAdmissionsUndergraduate() : ResponseEntity<List<AdmissionsDto>> {
        return ResponseEntity.ok(admissionsService.readAdmissionsUndergraduate())
    }
}