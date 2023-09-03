package com.wafflestudio.csereal.core.scholarship.api

import com.wafflestudio.csereal.core.scholarship.dto.ScholarshipDto
import com.wafflestudio.csereal.core.scholarship.service.ScholarshipService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/scholarship")
@RestController
class ScholarshipController(
    private val scholarshipService: ScholarshipService
) {

    @GetMapping("/{scholarshipId}")
    fun getScholarship(@PathVariable scholarshipId: Long): ResponseEntity<ScholarshipDto> {
        return ResponseEntity.ok(scholarshipService.getScholarship(scholarshipId))
    }
}
