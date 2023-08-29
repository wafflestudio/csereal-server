package com.wafflestudio.csereal.core.academics.api

import com.wafflestudio.csereal.core.academics.dto.CourseDto
import com.wafflestudio.csereal.core.academics.dto.AcademicsDto
import com.wafflestudio.csereal.core.academics.service.AcademicsService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/academics")
@RestController
class AcademicsController(
    private val academicsService: AcademicsService
) {

    //Todo: 이미지, 파일 추가 필요
    @PostMapping("/{studentType}/{postType}")
    fun createAcademics(
        @PathVariable studentType: String,
        @PathVariable postType: String,
        @Valid @RequestPart("request") request: AcademicsDto,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ) : ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(academicsService.createAcademics(studentType, postType, request, attachments))
    }

    @GetMapping("/{studentType}/{postType}")
    fun readAcademics(
        @PathVariable studentType: String,
        @PathVariable postType: String,
    ): ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(academicsService.readAcademics(studentType, postType))
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
    ) : ResponseEntity<List<CourseDto>> {
        return ResponseEntity.ok(academicsService.readAllCourses(studentType))
    }

    @GetMapping("/course")
    fun readCourse(
        @RequestParam name: String
    ): ResponseEntity<CourseDto> {
        return ResponseEntity.ok(academicsService.readCourse(name))
    }

    // 장학금
    @PostMapping("/{studentType}/scholarship")
    fun createScholarship(
        @PathVariable studentType: String,
        @Valid @RequestPart("request") request: AcademicsDto,
        @RequestPart("attachments") attachments: List<MultipartFile>?,
    ) : ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(academicsService.createAcademics(studentType, "scholarship", request, attachments))
    }

    @GetMapping("/scholarship")
    fun readScholarship(
        @RequestParam name: String
    ) : ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(academicsService.readScholarship(name))
    }

}