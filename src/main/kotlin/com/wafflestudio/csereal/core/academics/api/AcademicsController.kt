package com.wafflestudio.csereal.core.academics.api

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.academics.dto.*
import com.wafflestudio.csereal.core.academics.service.AcademicsService
import com.wafflestudio.csereal.core.academics.dto.ScholarshipDto
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v1/academics")
@RestController
class AcademicsController(
    private val academicsService: AcademicsService
) {
    @AuthenticatedStaff
    @PostMapping("/{studentType}/{postType}")
    fun createAcademics(
        @PathVariable studentType: String,
        @PathVariable postType: String,
        @Valid
        @RequestPart("request")
        request: AcademicsDto,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ): ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(academicsService.createAcademics(studentType, postType, request, attachments))
    }

    @GetMapping("/{studentType}/guide")
    fun readGuide(
        @PathVariable studentType: String
    ): ResponseEntity<GuidePageResponse> {
        return ResponseEntity.ok(academicsService.readGuide(studentType))
    }

    @GetMapping("/{studentType}/{postType}")
    fun readAcademicsYearResponses(
        @PathVariable studentType: String,
        @PathVariable postType: String
    ): ResponseEntity<List<AcademicsYearResponse>> {
        return ResponseEntity.ok(academicsService.readAcademicsYearResponses(studentType, postType))
    }

    //교과목 정보
    @AuthenticatedStaff
    @PostMapping("/{studentType}/course")
    fun createCourse(
        @PathVariable studentType: String,
        @Valid
        @RequestPart("request")
        request: CourseDto,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ): ResponseEntity<CourseDto> {
        return ResponseEntity.ok(academicsService.createCourse(studentType, request, attachments))
    }

    @GetMapping("/{language}/{studentType}/courses")
    fun readAllCourses(
        @PathVariable language: String,
        @PathVariable studentType: String
    ): ResponseEntity<List<CourseDto>> {
        return ResponseEntity.ok(academicsService.readAllCourses(language, studentType))
    }

    @GetMapping("/{language}/course")
    fun readCourse(
        @PathVariable language: String,
        @RequestParam name: String
    ): ResponseEntity<CourseDto> {
        return ResponseEntity.ok(academicsService.readCourse(language, name))
    }

    @GetMapping("/undergraduate/general-studies-requirements")
    fun readGeneralStudiesRequirements(): ResponseEntity<GeneralStudiesPageResponse> {
        return ResponseEntity.ok(academicsService.readGeneralStudies())
    }

    @AuthenticatedStaff
    @PostMapping("/{studentType}/scholarshipDetail")
    fun createScholarshipDetail(
        @PathVariable studentType: String,
        @Valid @RequestBody
        request: ScholarshipDto
    ): ResponseEntity<ScholarshipDto> {
        return ResponseEntity.ok(academicsService.createScholarshipDetail(studentType, request))
    }

    @GetMapping("/{studentType}/scholarship")
    fun readAllScholarship(
        @PathVariable studentType: String
    ): ResponseEntity<ScholarshipPageResponse> {
        return ResponseEntity.ok(academicsService.readAllScholarship(studentType))
    }

    @GetMapping("/scholarship/{scholarshipId}")
    fun getScholarship(@PathVariable scholarshipId: Long): ResponseEntity<ScholarshipDto> {
        return ResponseEntity.ok(academicsService.readScholarship(scholarshipId))
    }
}
