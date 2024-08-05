package com.wafflestudio.csereal.core.academics.api.v2

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.academics.dto.GroupedCourseDto
import com.wafflestudio.csereal.core.academics.service.AcademicsService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/v2/academics")
@RestController
class AcademicsController(
    private val academicsService: AcademicsService
) {
    @AuthenticatedStaff
    @PostMapping("/{studentType}/course")
    fun createCourse(
        @PathVariable studentType: String,
        @Valid
        @RequestBody
        request: GroupedCourseDto
    ) = academicsService.createCourse(studentType, request)

    @GetMapping("/{studentType}/courses")
    fun readAllGroupedCourses(@PathVariable studentType: String): List<GroupedCourseDto> =
        academicsService.readAllGroupedCourses(studentType)
}
