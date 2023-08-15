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
    @PostMapping("/{postType}/{admissionsType}")
    fun createAdmissions(
        @PathVariable postType: String,
        @PathVariable admissionsType: String,
        @Valid @RequestBody request: AdmissionsDto
    ) : AdmissionsDto {
        return admissionsService.createAdmissions(postType, admissionsType, request)
    }

    /*
    @GetMapping("/{postType}")
    fun readAdmissionsUndergraduate(
        @PathVariable postType: String
    ) : ResponseEntity<List<AdmissionsDto>> {
        return ResponseEntity.ok(admissionsService.readAdmissionsUndergraduate(postType))
    }

     */

    @GetMapping("/{postType}")
    fun readAdmissions(
        @PathVariable postType: String
    ) : ResponseEntity<List<AdmissionsDto>> {
        return ResponseEntity.ok(admissionsService.readAdmissions(postType))
    }
}