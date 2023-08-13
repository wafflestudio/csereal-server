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
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/undergraduate")
@RestController
class UndergraduateController(
    private val undergraduateService: UndergraduateService
) {

    // postType -> 메인페이지: main, 필수 교양 과목: general-education-requirements,
    // 전공 이수 표준 형태: recommended-tracks (~2020년), 졸업 규청: degree-requirements (~2020년),
    // 교과목 변경 내역: course-changes, 장학제도: scholarships
    //Todo: 이미지, 파일 추가 필요
    @PostMapping
    fun createUndergraduate(
        @Valid @RequestBody request: UndergraduateDto
    ) : ResponseEntity<UndergraduateDto> {
        return ResponseEntity.ok(undergraduateService.createUndergraduate(request))
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
    @GetMapping("/courses")
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