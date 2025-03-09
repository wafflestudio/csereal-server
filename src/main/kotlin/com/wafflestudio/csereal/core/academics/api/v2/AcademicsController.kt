package com.wafflestudio.csereal.core.academics.api.v2

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.academics.api.req.*
import com.wafflestudio.csereal.core.academics.dto.*
import com.wafflestudio.csereal.core.academics.service.AcademicsSearchService
import com.wafflestudio.csereal.core.academics.service.AcademicsService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v2/academics")
@RestController
class AcademicsController(
    private val academicsService: AcademicsService,
    private val academicsSearchService: AcademicsSearchService
) {
    @PreAuthorize("hasRole('STAFF')")
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

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/courses")
    fun updateCourse(@RequestBody updateRequest: GroupedCourseDto) = academicsService.updateCourse(updateRequest)

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/courses/{code}")
    fun deleteCourse(@PathVariable code: String) = academicsService.deleteCourse(code)

    @PreAuthorize("hasRole('STAFF')")
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

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/scholarship")
    fun updateScholarship(@RequestBody request: UpdateScholarshipReq) = academicsService.updateScholarship(request)

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/scholarship/{scholarshipId}")
    fun deleteScholarship(@PathVariable scholarshipId: Long) = academicsService.deleteScholarship(scholarshipId)

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{studentType}/scholarship")
    fun updateScholarshipPage(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable studentType: String,
        @RequestBody request: UpdateScholarshipPageReq
    ) = academicsService.updateScholarshipPage(language, studentType, request)

    @GetMapping("/{studentType}/guide")
    fun readGuide(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable studentType: String
    ): ResponseEntity<GuidePageResponse> {
        return ResponseEntity.ok(academicsService.readGuide(language, studentType))
    }

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{studentType}/guide")
    fun updateGuide(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable studentType: String,
        @RequestPart request: UpdateSingleReq,
        @RequestPart newAttachments: List<MultipartFile>?
    ) = academicsService.updateGuide(language, studentType, request, newAttachments)

    @GetMapping("/{studentType}/{postType}")
    fun readAcademicsYearResponses(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable studentType: String,
        @PathVariable postType: String
    ): ResponseEntity<List<AcademicsYearResponse>> {
        return ResponseEntity.ok(
            academicsService.readAcademicsYearResponses(language, studentType, postType)
        )
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/{studentType}/{postType}")
    fun createAcademicsYearResponse(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable studentType: String,
        @PathVariable postType: String,
        @RequestPart request: CreateYearReq,
        @RequestPart attachments: List<MultipartFile>?
    ) = academicsService.createAcademicsYearResponse(language, studentType, postType, request, attachments)

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{studentType}/{postType}/{year}")
    fun updateAcademicsYearResponse(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable studentType: String,
        @PathVariable postType: String,
        @PathVariable year: Int,
        @RequestPart request: UpdateYearReq,
        @RequestPart newAttachments: List<MultipartFile>?
    ) = academicsService.updateAcademicsYearResponse(language, studentType, postType, year, request, newAttachments)

    @GetMapping("/undergraduate/degree-requirements")
    fun readDegreeRequirements(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<DegreeRequirementsPageResponse> {
        return ResponseEntity.ok(academicsService.readDegreeRequirements(language))
    }

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/undergraduate/degree-requirements")
    fun updateDegreeRequirements(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @RequestPart request: UpdateSingleReq,
        @RequestPart newAttachments: List<MultipartFile>?
    ) = academicsService.updateDegreeRequirements(language, request, newAttachments)

    @GetMapping("/{studentType}/scholarship")
    fun readAllScholarship(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable studentType: String
    ): ResponseEntity<ScholarshipPageResponse> {
        return ResponseEntity.ok(academicsService.readAllScholarship(language, studentType))
    }

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/{studentType}/{postType}/{year}")
    fun deleteAcademicsYearResponse(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable studentType: String,
        @PathVariable postType: String,
        @PathVariable year: Int
    ) = academicsService.deleteAcademicsYearResponse(language, studentType, postType, year)

    @GetMapping("/search/top")
    fun searchTop(
        @RequestParam(required = true) keyword: String,
        @RequestParam(required = true) @Valid @Positive number: Int,
        @RequestParam(required = true, defaultValue = "ko") language: String,
        @RequestParam(required = false, defaultValue = "30") amount: Int
    ) = academicsSearchService.searchTopAcademics(
        keyword = keyword,
        language = LanguageType.makeStringToLanguageType(language),
        number = number,
        amount = amount
    )

    @GetMapping("/search")
    fun searchAcademics(
        @RequestParam(required = true) keyword: String,
        @RequestParam(required = true) @Valid @Positive pageSize: Int,
        @RequestParam(required = true) @Valid @Positive pageNum: Int,
        @RequestParam(required = true, defaultValue = "ko") language: String,
        @RequestParam(required = false, defaultValue = "30") amount: Int
    ) = academicsSearchService.searchAcademics(
        keyword = keyword,
        language = LanguageType.makeStringToLanguageType(language),
        pageSize = pageSize,
        pageNum = pageNum,
        amount = amount
    )
}
