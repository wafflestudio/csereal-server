package com.wafflestudio.csereal.core.academics.api.v2

import com.wafflestudio.csereal.core.academics.dto.GroupedCourseDto
import com.wafflestudio.csereal.core.academics.service.AcademicsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v2/academics")
@RestController
class AcademicsController(
    private val academicsService: AcademicsService
) {
    @GetMapping("/{studentType}/courses")
    fun readAllGroupedCourses(@PathVariable studentType: String): List<GroupedCourseDto> =
        academicsService.readAllGroupedCourses(studentType)
}
