package com.wafflestudio.csereal.core.academics.api

import com.wafflestudio.csereal.core.academics.database.StudentType
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
    @PostMapping("/{studentType}")
    fun createAcademics(
        @PathVariable studentType: StudentType,
        @Valid @RequestBody request: AcademicsDto
    ) : ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(academicsService.createAcademics(studentType, request))
    }

    @GetMapping("/{studentType}/{postType}")
    fun readAcademics(
        @PathVariable studentType: StudentType,
        @PathVariable postType: String,
    ): ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(academicsService.readAcademics(studentType, postType))
    }

    //교과목 정보: courses
    @PostMapping("/{studentType}/course")
    fun createCourse(
        @PathVariable studentType: StudentType,
        @Valid @RequestBody request: CourseDto
    ) : ResponseEntity<CourseDto> {
        return ResponseEntity.ok(academicsService.createCourse(studentType, request))
    }

    @GetMapping("/{studentType}/courses")
    fun readAllCourses(
        @PathVariable studentType: StudentType,
    ) : ResponseEntity<List<CourseDto>> {
        return ResponseEntity.ok(academicsService.readAllCourses(studentType))
    }

    @GetMapping("/course/{name}")
    fun readCourse(
        @PathVariable name: String
    ): ResponseEntity<CourseDto> {
        return ResponseEntity.ok(academicsService.readCourse(name))
    }

    // 장학금
    @PostMapping("/{studentType}/scholarship")
    fun createScholarship(
        @PathVariable studentType: StudentType,
        @Valid @RequestBody request: AcademicsDto
    ) : ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(academicsService.createAcademics(studentType, request))
    }

    @GetMapping("/scholarship/{name}")
    fun readScholarship(
        @PathVariable name: String
    ) : ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(academicsService.readScholarship(name))
    }

}