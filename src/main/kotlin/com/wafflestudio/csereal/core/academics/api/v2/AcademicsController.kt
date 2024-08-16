package com.wafflestudio.csereal.core.academics.api.v2

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.academics.api.req.CreateScholarshipReq
import com.wafflestudio.csereal.core.academics.api.req.UpdateScholarshipReq
import com.wafflestudio.csereal.core.academics.dto.GroupedCourseDto
import com.wafflestudio.csereal.core.academics.dto.ScholarshipDto
import com.wafflestudio.csereal.core.academics.service.AcademicsService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/v2/academics")
@RestController
class AcademicsController(
    private val academicsService: AcademicsService
) {
    @AuthenticatedStaff
    @PostMapping("/courses")
    fun createCourse(
        @Valid
        @RequestBody
        request: GroupedCourseDto
    ) = academicsService.createCourse(request)

    @GetMapping("/courses")
    fun readAllGroupedCourses(
        @RequestParam studentType: String,
        @RequestParam(required = false, defaultValue = "ko") sort: String
    ): List<GroupedCourseDto> =
        academicsService.readAllGroupedCourses(studentType, sort)

    @AuthenticatedStaff
    @PutMapping("/courses")
    fun updateCourse(@RequestBody updateRequest: GroupedCourseDto) = academicsService.updateCourse(updateRequest)

    @AuthenticatedStaff
    @DeleteMapping("/courses/{code}")
    fun deleteCourse(@PathVariable code: String) = academicsService.deleteCourse(code)

    @AuthenticatedStaff
    @PostMapping("/{studentType}/scholarship")
    fun createScholarship(
        @PathVariable studentType: String,
        @Valid @RequestBody
        request: CreateScholarshipReq
    ) = academicsService.createScholarship(studentType, request)

    @GetMapping("/scholarship/{scholarshipId}")
    fun getScholarship(
        @PathVariable scholarshipId: Long
    ): Pair<ScholarshipDto, ScholarshipDto> = academicsService.readScholarshipV2(scholarshipId)

    @AuthenticatedStaff
    @PutMapping("/scholarship")
    fun updateScholarship(@RequestBody request: UpdateScholarshipReq) = academicsService.updateScholarship(request)

    @AuthenticatedStaff
    @DeleteMapping("/scholarship/{scholarshipId}")
    fun deleteScholarship(@PathVariable scholarshipId: Long) = academicsService.deleteScholarship(scholarshipId)
}
