package com.wafflestudio.csereal.core.academics.api.v2

import com.wafflestudio.csereal.core.academics.database.AcademicsStudentType
import com.wafflestudio.csereal.core.academics.database.AcademicsYearPostType
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.academics.api.req.*
import com.wafflestudio.csereal.core.academics.dto.*
import com.wafflestudio.csereal.core.academics.service.AcademicsSearchService
import com.wafflestudio.csereal.core.academics.service.AcademicsService
import jakarta.validation.Valid
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
        @RequestParam studentType: AcademicsStudentType,
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
        @PathVariable studentType: AcademicsStudentType,
        @Valid @RequestBody
        request: CreateScholarshipReq
    ) = academicsService.createScholarship(studentType, request)

    @GetMapping("/scholarship/{scholarshipId}")
    fun getScholarship(
        @PathVariable scholarshipId: Long
    ): ScholarshipLanguagesDto = academicsService.readScholarshipV2(scholarshipId)

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/scholarship/{scholarshipId}")
    fun updateScholarship(
        @PathVariable scholarshipId: Long,
        @RequestBody request: UpdateScholarshipReq
    ) = academicsService.updateScholarship(scholarshipId, request)

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/scholarship/{scholarshipId}")
    fun deleteScholarship(@PathVariable scholarshipId: Long) = academicsService.deleteScholarship(scholarshipId)

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{studentType}/scholarship")
    fun updateScholarshipPage(
        @RequestParam(required = false, defaultValue = "ko") language: LanguageType,
        @PathVariable studentType: AcademicsStudentType,
        @RequestBody request: UpdateScholarshipPageReq
    ) = academicsService.updateScholarshipPage(language, studentType, request)

    @GetMapping("/{studentType}/guide")
    fun readGuide(
        @RequestParam(required = false, defaultValue = "ko") language: LanguageType,
        @PathVariable studentType: AcademicsStudentType
    ): ResponseEntity<GuidePageResponse> {
        return ResponseEntity.ok(academicsService.readGuide(language, studentType))
    }

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{studentType}/guide", consumes = ["multipart/form-data"])
    fun updateGuide(
        @RequestParam(required = false, defaultValue = "ko") language: LanguageType,
        @PathVariable studentType: AcademicsStudentType,
        @RequestPart request: UpdateSingleReq,
        @RequestPart attachments: List<MultipartFile>?
    ) = academicsService.updateGuide(language, studentType, request, attachments)

    // 연도별 목록을 갖는 셋만 받는다 — guide·scholarship·degree-requirements 는
    // 전용 경로가 이긴다. 자세한 이유는 AcademicsYearPostType 주석 참고.
    @GetMapping("/{studentType}/{postType}")
    fun readAcademicsYearResponses(
        @RequestParam(required = false, defaultValue = "ko") language: LanguageType,
        @PathVariable studentType: AcademicsStudentType,
        @PathVariable postType: AcademicsYearPostType
    ): ResponseEntity<List<AcademicsYearResponse>> {
        return ResponseEntity.ok(
            academicsService.readAcademicsYearResponses(language, studentType, postType.postType)
        )
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/{studentType}/{postType}", consumes = ["multipart/form-data"])
    fun createAcademicsYearResponse(
        @RequestParam(required = false, defaultValue = "ko") language: LanguageType,
        @PathVariable studentType: AcademicsStudentType,
        @PathVariable postType: AcademicsYearPostType,
        @RequestPart request: CreateYearReq,
        @RequestPart attachments: List<MultipartFile>?
    ) = academicsService.createAcademicsYearResponse(
        language,
        studentType,
        postType.postType,
        request,
        attachments
    )

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{studentType}/{postType}/{year}", consumes = ["multipart/form-data"])
    fun updateAcademicsYearResponse(
        @RequestParam(required = false, defaultValue = "ko") language: LanguageType,
        @PathVariable studentType: AcademicsStudentType,
        @PathVariable postType: AcademicsYearPostType,
        @PathVariable year: Int,
        @RequestPart request: UpdateYearReq,
        @RequestPart attachments: List<MultipartFile>?
    ) = academicsService.updateAcademicsYearResponse(
        language,
        studentType,
        postType.postType,
        year,
        request,
        attachments
    )

    @GetMapping("/undergraduate/degree-requirements")
    fun readDegreeRequirements(
        @RequestParam(required = false, defaultValue = "ko") language: LanguageType
    ): ResponseEntity<DegreeRequirementsPageResponse> {
        return ResponseEntity.ok(academicsService.readDegreeRequirements(language))
    }

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/undergraduate/degree-requirements", consumes = ["multipart/form-data"])
    fun updateDegreeRequirements(
        @RequestParam(required = false, defaultValue = "ko") language: LanguageType,
        @RequestPart request: UpdateSingleReq,
        @RequestPart attachments: List<MultipartFile>?
    ) = academicsService.updateDegreeRequirements(language, request, attachments)

    @GetMapping("/{studentType}/scholarship")
    fun readAllScholarship(
        @RequestParam(required = false, defaultValue = "ko") language: LanguageType,
        @PathVariable studentType: AcademicsStudentType
    ): ResponseEntity<ScholarshipPageResponse> {
        return ResponseEntity.ok(academicsService.readAllScholarship(language, studentType))
    }

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/{studentType}/{postType}/{year}")
    fun deleteAcademicsYearResponse(
        @RequestParam(required = false, defaultValue = "ko") language: LanguageType,
        @PathVariable studentType: AcademicsStudentType,
        @PathVariable postType: AcademicsYearPostType,
        @PathVariable year: Int
    ) = academicsService.deleteAcademicsYearResponse(language, studentType, postType.postType, year)
}
