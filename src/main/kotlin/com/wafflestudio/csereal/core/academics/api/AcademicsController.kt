package com.wafflestudio.csereal.core.academics.api

import com.wafflestudio.csereal.core.academics.dto.*
import com.wafflestudio.csereal.core.academics.service.AcademicsService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v1/academics")
@RestController
class AcademicsController(
    private val academicsService: AcademicsService
) {
    @PostMapping("/{studentType}/{postType}")
    fun createAcademics(
        @PathVariable studentType: String,
        @PathVariable postType: String,
        @Valid @RequestPart("request") request: AcademicsDto,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ) : ResponseEntity<AcademicsDto> {
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
        @PathVariable postType: String,
    ): ResponseEntity<List<AcademicsYearResponse>> {
        return ResponseEntity.ok(academicsService.readAcademicsYearResponses(studentType, postType))
    }

    //교과목 정보
    @PostMapping("/{studentType}/course")
    fun createCourse(
        @PathVariable studentType: String,
        @Valid @RequestPart("request") request: CourseDto,
        @RequestPart("attachments") attachments: List<MultipartFile>?,
    ) : ResponseEntity<CourseDto> {
        return ResponseEntity.ok(academicsService.createCourse(studentType, request, attachments))
    }

    @GetMapping("/{studentType}/courses")
    fun readAllCourses(
        @PathVariable studentType: String,
    ): ResponseEntity<List<CourseDto>> {
        return ResponseEntity.ok(academicsService.readAllCourses(studentType))
    }

    @GetMapping("/course")
    fun readCourse(
        @RequestParam name: String
    ): ResponseEntity<CourseDto> {
        return ResponseEntity.ok(academicsService.readCourse(name))
    }

    @GetMapping("/{studentType}/course-changes")
    fun readCourseChanges(
        @PathVariable studentType: String,
    ) : ResponseEntity<List<AcademicsYearResponse>> {
        return ResponseEntity.ok(academicsService.readAcademicsYearResponses(studentType, "course-changes"))
    }

    @GetMapping("/scholarship")
    fun readScholarship(
        @RequestParam name: String
    ): ResponseEntity<ScholarshipPageResponse> {
        return ResponseEntity.ok(academicsService.readScholarship(name))
    }

}
