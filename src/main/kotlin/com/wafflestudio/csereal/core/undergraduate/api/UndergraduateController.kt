package com.wafflestudio.csereal.core.undergraduate.api

import com.wafflestudio.csereal.core.undergraduate.dto.CourseDto
import com.wafflestudio.csereal.core.undergraduate.dto.UndergraduateDto
import com.wafflestudio.csereal.core.undergraduate.service.UndergraduateService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/undergraduate")
@RestController
class UndergraduateController(
    private val undergraduateService: UndergraduateService
) {
    // 학부 메인 페이지
    @PostMapping
    fun createUndergraduateMain(
        @Valid @RequestBody request: UndergraduateDto
    ) : ResponseEntity<UndergraduateDto> {
        return ResponseEntity.ok(undergraduateService.createUndergraduate("main", request))
    }

    @GetMapping
    fun readUndergraduateMain() : ResponseEntity<UndergraduateDto> {
        return ResponseEntity.ok(undergraduateService.readUndergraduate("main"))
    }

    // postType -> 필수 교양 과목: general-education-requirements,
    // 전공 이수 표준 형태: recommended-tracks (~2020년), 졸업 규청: degree-requirements (~2020년),
    // 교과목 변경 내역: course-changes, 장학제도: scholarships
    //Todo: 선수 교과목 -> 파일/이미지 필요, 전공 이수 표준 형태(2021년~) -> 파일 필요,
    //Todo: 졸업 규정(2021년~) -> 파일 필요
    @PostMapping("/{postType}")
    fun createUndergraduate(
        @PathVariable postType: String,
        @Valid @RequestBody request: UndergraduateDto
    ) : ResponseEntity<UndergraduateDto> {
        return ResponseEntity.ok(undergraduateService.createUndergraduate(postType, request))
    }

    @GetMapping("/{postType}")
    fun readUndergraduate(
        @PathVariable postType: String,
    ): ResponseEntity<UndergraduateDto> {
        return ResponseEntity.ok(undergraduateService.readUndergraduate(postType))
    }

    /*
    @PostMapping
    fun createUnderCourseDependency(
        @Valid @RequestBody request: UndergraduateDto
    ) : ResponseEntity<UndergraduateDto> {
        return ResponseEntity.ok(undergraduateService.createUnderCourseDependency(request))
    }

    @GetMapping
    fun readUnderCourseDependency(): ResponseEntity<UndergraduateDto> {
        return ResponseEntity.ok(undergraduateService.readUnderCourseDependency())
    }

    */

    //교과목 정보
    @GetMapping("/course")
    fun readAllCourses() : ResponseEntity<List<CourseDto>> {
        return ResponseEntity.ok(undergraduateService.readAllCourses())
    }
    @PostMapping("/course")
    fun createCourse(
        @Valid @RequestBody request: CourseDto
    ) : ResponseEntity<CourseDto> {
        return ResponseEntity.ok(undergraduateService.createCourse(request))
    }

    @GetMapping("/course/{title}")
    fun readCourse(
        @PathVariable title: String
    ): ResponseEntity<CourseDto> {
        return ResponseEntity.ok(undergraduateService.readCourse(title))
    }

}