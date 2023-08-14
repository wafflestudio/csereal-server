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
import org.springframework.web.bind.annotation.RestController

@RequestMapping
@RestController
class AcademicsController(
    private val academicsService: AcademicsService
) {

    // postType -> 학부 안내: guide, 필수 교양 과목: general-studies-requirements,
    // 전공 이수 표준 형태: curriculum, 졸업 규청: degree-requirements,
    // 교과목 변경 내역: course-changes, 장학제도: scholarship
    //Todo: 이미지, 파일 추가 필요
    @PostMapping("/{to}")
    fun createAcademics(
        @PathVariable to: String,
        @Valid @RequestBody request: AcademicsDto
    ) : ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(academicsService.createAcademics(to, request))
    }

    @GetMapping("/{to}/{postType}")
    fun readAcademics(
        @PathVariable to: String,
        @PathVariable postType: String,
    ): ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(academicsService.readAcademics(to, postType))
    }

    /*
    @PostMapping
    fun createUnderCourseDependency(
        @Valid @RequestBody request: AcademicsDto
    ) : ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(AcademicsService.createUnderCourseDependency(request))
    }

    @GetMapping
    fun readUnderCourseDependency(): ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(AcademicsService.readUnderCourseDependency())
    }

    */

    //교과목 정보: courses

    @PostMapping("/{to}/course")
    fun createCourse(
        @PathVariable to: String,
        @Valid @RequestBody request: CourseDto
    ) : ResponseEntity<CourseDto> {
        return ResponseEntity.ok(academicsService.createCourse(to, request))
    }

    @GetMapping("/{to}/courses")
    fun readAllCourses(
        @PathVariable to: String,
    ) : ResponseEntity<List<CourseDto>> {
        return ResponseEntity.ok(academicsService.readAllCourses(to))
    }

    @GetMapping("/course/{name}")
    fun readCourse(
        @PathVariable name: String
    ): ResponseEntity<CourseDto> {
        return ResponseEntity.ok(academicsService.readCourse(name))
    }

    // 장학금
    @PostMapping("/{to}/scholarship")
    fun createScholarship(
        @PathVariable to: String,
        @Valid @RequestBody request: AcademicsDto
    ) : ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(academicsService.createAcademics(to, request))
    }

    @GetMapping("/scholarship/{name}")
    fun readScholarship(
        @PathVariable name: String
    ) : ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(academicsService.readScholarship(name))
    }

}