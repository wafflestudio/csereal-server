package com.wafflestudio.csereal.core.academics.api

import com.wafflestudio.csereal.core.academics.dto.CourseDto
import com.wafflestudio.csereal.core.academics.dto.AcademicsDto
import com.wafflestudio.csereal.core.academics.service.AcademicsService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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
        @Valid @RequestBody request: AcademicsDto
    ) : ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(academicsService.createAcademics(studentType, postType, request))
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
        @Valid @RequestBody request: CourseDto
    ) : ResponseEntity<CourseDto> {
        return ResponseEntity.ok(academicsService.createCourse(studentType, request))
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
        @Valid @RequestBody request: AcademicsDto
    ) : ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(academicsService.createAcademics(studentType, "scholarship", request))
    }

    @GetMapping("/scholarship")
    fun readScholarship(
        @RequestParam name: String
    ) : ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(academicsService.readScholarship(name))
    }

}