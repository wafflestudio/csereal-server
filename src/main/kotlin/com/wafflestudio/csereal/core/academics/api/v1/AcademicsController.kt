package com.wafflestudio.csereal.core.academics.api.v1

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.academics.api.req.CreateYearReq
import com.wafflestudio.csereal.core.academics.api.req.UpdateSingleReq
import com.wafflestudio.csereal.core.academics.api.req.UpdateYearReq
import com.wafflestudio.csereal.core.academics.dto.*
import com.wafflestudio.csereal.core.academics.service.AcademicsService
import com.wafflestudio.csereal.core.academics.dto.ScholarshipDto
import com.wafflestudio.csereal.core.academics.service.AcademicsSearchService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v1/academics")
@RestController("AcademicsControllerV1")
class AcademicsController(
    private val academicsService: AcademicsService,
    private val academicsSearchService: AcademicsSearchService
) {

    @GetMapping("/{studentType}/guide")
    fun readGuide(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable studentType: String
    ): ResponseEntity<GuidePageResponse> {
        return ResponseEntity.ok(academicsService.readGuide(language, studentType))
    }

    @AuthenticatedStaff
    @PutMapping("/{studentType}/guide")
    fun updateGuide(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable studentType: String,
        @RequestPart request: UpdateSingleReq,
        @RequestPart newAttachments: List<MultipartFile>?
    ) = academicsService.updateGuide(language, studentType, request, newAttachments)

    @GetMapping("/undergraduate/general-studies-requirements")
    fun readGeneralStudiesRequirements(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<GeneralStudiesRequirementsPageResponse> {
        return ResponseEntity.ok(academicsService.readGeneralStudiesRequirements(language))
    }

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

    @AuthenticatedStaff
    @PostMapping("/{studentType}/{postType}")
    fun createAcademicsYearResponse(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable studentType: String,
        @PathVariable postType: String,
        @RequestBody request: CreateYearReq
    ) = academicsService.createAcademicsYearResponse(language, studentType, postType, request)

    @AuthenticatedStaff
    @PutMapping("/{studentType}/{postType}/{year}")
    fun updateAcademicsYearResponse(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable studentType: String,
        @PathVariable postType: String,
        @PathVariable year: Int,
        @RequestBody request: UpdateYearReq
    ) = academicsService.updateAcademicsYearResponse(language, studentType, postType, year, request)

    @AuthenticatedStaff
    @DeleteMapping("/{studentType}/{postType}/{year}")
    fun deleteAcademicsYearResponse(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable studentType: String,
        @PathVariable postType: String,
        @PathVariable year: Int
    ) = academicsService.deleteAcademicsYearResponse(language, studentType, postType, year)

    //교과목 정보
    @Deprecated(message = "Use V2 API")
    @GetMapping("/{studentType}/courses")
    fun readAllCourses(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable studentType: String
    ): ResponseEntity<List<CourseDto>> {
        return ResponseEntity.ok(academicsService.readAllCourses(language, studentType))
    }

    @GetMapping("/undergraduate/degree-requirements")
    fun readDegreeRequirements(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<DegreeRequirementsPageResponse> {
        return ResponseEntity.ok(academicsService.readDegreeRequirements(language))
    }

    @AuthenticatedStaff
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

    @Deprecated(message = "Use V2 API")
    @GetMapping("/scholarship/{scholarshipId}")
    fun getScholarship(
        @PathVariable scholarshipId: Long
    ): ResponseEntity<ScholarshipDto> {
        return ResponseEntity.ok(academicsService.readScholarship(scholarshipId))
    }

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
